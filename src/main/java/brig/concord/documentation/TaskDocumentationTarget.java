package brig.concord.documentation;

import brig.concord.schema.SchemaType;
import brig.concord.schema.TaskSchema;
import brig.concord.schema.TaskSchemaProperty;
import brig.concord.schema.TaskSchemaSection;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class TaskDocumentationTarget implements DocumentationTarget {

    private static final int SMART_PARAM_THRESHOLD = 10;

    private final SmartPsiElementPointer<YAMLKeyValue> taskPointer;
    private final String taskName;
    private final @Nullable String description;
    private final List<ParamInfo> inputParams;
    private final List<ParamInfo> outputParams;
    private final @Nullable String inputFooter;
    private final List<ConditionalGroup> inputGroups;

    public TaskDocumentationTarget(@NotNull YAMLKeyValue taskKv, @NotNull TaskSchema schema) {
        this.taskPointer = SmartPointerManager.createPointer(taskKv);
        this.taskName = schema.getTaskName();
        this.description = schema.getDescription();

        var inputResult = buildInputParams(schema);
        this.inputParams = inputResult.params;
        this.inputFooter = inputResult.footer;
        this.inputGroups = inputResult.groups;

        this.outputParams = toParamInfos(schema.getOutSection());
    }

    private TaskDocumentationTarget(@NotNull YAMLKeyValue taskKv,
                                    @NotNull String taskName,
                                    @Nullable String description,
                                    @NotNull List<ParamInfo> inputParams,
                                    @Nullable String inputFooter,
                                    @NotNull List<ConditionalGroup> inputGroups,
                                    @NotNull List<ParamInfo> outputParams) {
        this.taskPointer = SmartPointerManager.createPointer(taskKv);
        this.taskName = taskName;
        this.description = description;
        this.inputParams = inputParams;
        this.inputFooter = inputFooter;
        this.inputGroups = inputGroups;
        this.outputParams = outputParams;
    }

    @Override
    public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
        var ptr = this.taskPointer;
        var name = this.taskName;
        var desc = this.description;
        var inParams = this.inputParams;
        var footer = this.inputFooter;
        var groups = this.inputGroups;
        var outParams = this.outputParams;
        return () -> {
            var element = ptr.getElement();
            return element == null ? null : new TaskDocumentationTarget(element, name, desc, inParams, footer, groups, outParams);
        };
    }

    @Override
    public @NotNull TargetPresentation computePresentation() {
        return TargetPresentation.builder(taskName).presentation();
    }

    @Override
    public @Nullable String computeDocumentationHint() {
        return description;
    }

    @Override
    public @Nullable DocumentationResult computeDocumentation() {
        var element = taskPointer.getElement();
        if (element == null) {
            return null;
        }

        var sb = new StringBuilder();

        sb.append(DEFINITION_START)
                .append(StringUtil.escapeXmlEntities(taskName))
                .append(DEFINITION_END);

        sb.append(CONTENT_START);

        if (description != null) {
            sb.append("<p>").append(StringUtil.decapitalize(description)).append("</p>");
        }

        appendParamSection(sb, "Input Parameters:", inputParams, inputFooter, inputGroups);
        appendParamSection(sb, "Output Parameters:", outputParams, null, List.of());

        sb.append(CONTENT_END);

        return DocumentationResult.documentation(sb.toString());
    }

    private static void appendParamSection(StringBuilder sb, String title, List<ParamInfo> params,
                                           @Nullable String footer,
                                           @NotNull List<ConditionalGroup> groups) {
        if (params.isEmpty() && footer == null && groups.isEmpty()) {
            return;
        }

        sb.append("<p><b>").append(title).append("</b></p>");
        if (!params.isEmpty()) {
            appendParamList(sb, params);
        }
        for (var group : groups) {
            sb.append("<p><i>").append(group.header).append("</i></p>");
            appendParamList(sb, group.params);
        }
        if (footer != null) {
            sb.append("<p><i>").append(footer).append("</i></p>");
        }
    }

    private static void appendParamList(StringBuilder sb, List<ParamInfo> params) {
        sb.append("<ul>");
        for (var param : params) {
            sb.append("<li><code>").append(StringUtil.escapeXmlEntities(param.name)).append("</code>");
            if (param.type != null) {
                sb.append(" <i>(").append(StringUtil.escapeXmlEntities(param.type));
                if (param.required) {
                    sb.append(", required");
                }
                sb.append(")</i>");
            }
            if (param.description != null) {
                sb.append(" &mdash; ").append(StringUtil.decapitalize(param.description));
            }
            if (!param.enumValues.isEmpty()) {
                sb.append("<ul>");
                for (var ev : param.enumValues) {
                    sb.append("<li><code>").append(StringUtil.escapeXmlEntities(ev.name)).append("</code>");
                    if (ev.description != null) {
                        sb.append(" &mdash; ").append(StringUtil.decapitalize(ev.description));
                    }
                    sb.append("</li>");
                }
                sb.append("</ul>");
            }
            sb.append("</li>");
        }
        sb.append("</ul>");
    }

    private static InputResult buildInputParams(@NotNull TaskSchema schema) {
        var conditionals = schema.getInConditionals();
        if (conditionals.isEmpty()) {
            return new InputResult(toParamInfos(schema.getBaseInSection()), null, List.of());
        }

        // Count total unique input params across base + all conditionals
        var allParamNames = new LinkedHashSet<>(schema.getBaseInSection().properties().keySet());
        for (var conditional : conditionals) {
            allParamNames.addAll(conditional.thenSection().properties().keySet());
        }

        if (allParamNames.size() > SMART_PARAM_THRESHOLD) {
            // Show only discriminator parameters from the base section
            var discriminatorKeys = schema.getDiscriminatorKeys();
            var discriminatorParams = new ArrayList<ParamInfo>();
            var baseProps = schema.getBaseInSection().properties();
            for (var key : discriminatorKeys) {
                var prop = baseProps.get(key);
                if (prop != null) {
                    discriminatorParams.add(toParamInfo(prop));
                }
            }

            var footer = "Additional parameters depend on the value of "
                    + formatDiscriminatorKeysHtml(discriminatorKeys);
            return new InputResult(discriminatorParams, footer, List.of());
        }

        // Under threshold: show base params, then group conditional params
        var baseParams = toParamInfos(schema.getBaseInSection());
        var baseParamNames = schema.getBaseInSection().properties().keySet();

        var groups = new ArrayList<ConditionalGroup>();
        for (var conditional : conditionals) {
            var condParams = new ArrayList<ParamInfo>();
            for (var prop : conditional.thenSection().properties().values()) {
                if (!baseParamNames.contains(prop.name())) {
                    condParams.add(toParamInfo(prop));
                }
            }
            if (!condParams.isEmpty()) {
                groups.add(new ConditionalGroup(formatConditionHeader(conditional.discriminators()), condParams));
            }
        }
        return new InputResult(baseParams, null, groups);
    }

    private static String formatConditionHeader(@NotNull Map<String, List<String>> discriminators) {
        return "When " + discriminators.entrySet().stream()
                .map(e -> StringUtil.escapeXmlEntities(e.getKey()) + " = " +
                        e.getValue().stream().map(StringUtil::escapeXmlEntities).collect(Collectors.joining("|")))
                .collect(Collectors.joining(", ")) + ":";
    }

    private static String formatDiscriminatorKeysHtml(Set<String> keys) {
        return keys.stream()
                .map(k -> "<code>" + StringUtil.escapeXmlEntities(k) + "</code>")
                .collect(Collectors.joining(", "));
    }

    private static List<ParamInfo> toParamInfos(@NotNull TaskSchemaSection section) {
        return section.properties().values().stream()
                .map(TaskDocumentationTarget::toParamInfo)
                .toList();
    }

    private static ParamInfo toParamInfo(@NotNull TaskSchemaProperty prop) {
        return new ParamInfo(
                prop.name(),
                schemaTypeDisplayName(prop.schemaType()),
                prop.required(),
                prop.description(),
                extractEnumValues(prop.schemaType())
        );
    }

    private static @NotNull List<EnumValueInfo> extractEnumValues(@NotNull SchemaType schemaType) {
        if (!(schemaType instanceof SchemaType.Enum e)) {
            return List.of();
        }
        var values = e.values();
        var descriptions = e.descriptions();
        var result = new ArrayList<EnumValueInfo>(values.size());
        for (int i = 0; i < values.size(); i++) {
            var desc = i < descriptions.size() ? descriptions.get(i) : null;
            result.add(new EnumValueInfo(values.get(i), desc != null && !desc.isEmpty() ? desc : null));
        }
        return List.copyOf(result);
    }

    private static @NotNull String schemaTypeDisplayName(@NotNull SchemaType schemaType) {
        return switch (schemaType) {
            case SchemaType.Scalar s -> s.typeName();
            case SchemaType.Array a -> {
                var itemType = a.itemType();
                yield (itemType != null ? itemType : "any") + "[]";
            }
            case SchemaType.Enum e -> "enum";
            case SchemaType.Composite c -> c.alternatives().stream()
                    .map(TaskDocumentationTarget::schemaTypeDisplayName)
                    .collect(Collectors.joining("|"));
            case SchemaType.Any a -> "any";
        };
    }

    private record ConditionalGroup(@NotNull String header, @NotNull List<ParamInfo> params) {}

    private record InputResult(List<ParamInfo> params, @Nullable String footer,
                                @NotNull List<ConditionalGroup> groups) {}

    private record ParamInfo(@NotNull String name, @Nullable String type, boolean required,
                     @Nullable String description, @NotNull List<EnumValueInfo> enumValues) {
    }

    private record EnumValueInfo(@NotNull String name, @Nullable String description) {
    }
}
