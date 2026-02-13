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

    public TaskDocumentationTarget(@NotNull YAMLKeyValue taskKv, @NotNull TaskSchema schema) {
        this.taskPointer = SmartPointerManager.createPointer(taskKv);
        this.taskName = schema.getTaskName();
        this.description = schema.getDescription();

        var inputResult = buildInputParams(schema);
        this.inputParams = inputResult.params;
        this.inputFooter = inputResult.footer;

        this.outputParams = toParamInfos(schema.getOutSection());
    }

    private TaskDocumentationTarget(@NotNull YAMLKeyValue taskKv,
                                    @NotNull String taskName,
                                    @Nullable String description,
                                    @NotNull List<ParamInfo> inputParams,
                                    @Nullable String inputFooter,
                                    @NotNull List<ParamInfo> outputParams) {
        this.taskPointer = SmartPointerManager.createPointer(taskKv);
        this.taskName = taskName;
        this.description = description;
        this.inputParams = inputParams;
        this.inputFooter = inputFooter;
        this.outputParams = outputParams;
    }

    @Override
    public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
        var ptr = this.taskPointer;
        var name = this.taskName;
        var desc = this.description;
        var inParams = this.inputParams;
        var footer = this.inputFooter;
        var outParams = this.outputParams;
        return () -> {
            var element = ptr.getElement();
            return element == null ? null : new TaskDocumentationTarget(element, name, desc, inParams, footer, outParams);
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
            sb.append("<p>").append(StringUtil.escapeXmlEntities(description)).append("</p>");
        }

        appendParamSection(sb, "Input Parameters:", inputParams, inputFooter);
        appendParamSection(sb, "Output Parameters:", outputParams, null);

        sb.append(CONTENT_END);

        return DocumentationResult.documentation(sb.toString());
    }

    private static void appendParamSection(StringBuilder sb, String title, List<ParamInfo> params,
                                           @Nullable String footer) {
        if (params.isEmpty() && footer == null) {
            return;
        }

        sb.append("<p><b>").append(title).append("</b></p>");
        if (!params.isEmpty()) {
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
                    sb.append(" &mdash; ").append(StringUtil.escapeXmlEntities(param.description));
                }
                if (!param.enumValues.isEmpty()) {
                    sb.append("<ul>");
                    for (var ev : param.enumValues) {
                        sb.append("<li><code>").append(StringUtil.escapeXmlEntities(ev.name)).append("</code>");
                        if (ev.description != null) {
                            sb.append(" &mdash; ").append(StringUtil.escapeXmlEntities(ev.description));
                        }
                        sb.append("</li>");
                    }
                    sb.append("</ul>");
                }
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        if (footer != null) {
            sb.append("<p><i>").append(footer).append("</i></p>");
        }
    }

    private static InputResult buildInputParams(@NotNull TaskSchema schema) {
        var conditionals = schema.getInConditionals();
        if (conditionals.isEmpty()) {
            return new InputResult(toParamInfos(schema.getBaseInSection()), null);
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
                    + formatDiscriminatorKeys(discriminatorKeys);
            return new InputResult(discriminatorParams, footer);
        }

        return new InputResult(toParamInfos(schema.getBaseInSection()), null);
    }

    private static String formatDiscriminatorKeys(Set<String> keys) {
        var sb = new StringBuilder();
        var iter = keys.iterator();
        var first = true;
        while (iter.hasNext()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("<code>").append(StringUtil.escapeXmlEntities(iter.next())).append("</code>");
            first = false;
        }
        return sb.toString();
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

    static @NotNull String schemaTypeDisplayName(@NotNull SchemaType schemaType) {
        return switch (schemaType) {
            case SchemaType.Scalar s -> s.typeName();
            case SchemaType.Array a -> {
                var itemType = a.itemType();
                yield (itemType != null ? itemType : "any") + "[]";
            }
            case SchemaType.Enum e -> "enum";
            case SchemaType.Composite c -> {
                var sb = new StringBuilder();
                for (int i = 0; i < c.alternatives().size(); i++) {
                    if (i > 0) {
                        sb.append("|");
                    }
                    sb.append(schemaTypeDisplayName(c.alternatives().get(i)));
                }
                yield sb.toString();
            }
            case SchemaType.Any a -> "any";
        };
    }

    private record InputResult(List<ParamInfo> params, @Nullable String footer) {}

    record ParamInfo(@NotNull String name, @Nullable String type, boolean required,
                     @Nullable String description, @NotNull List<EnumValueInfo> enumValues) {
    }

    record EnumValueInfo(@NotNull String name, @Nullable String description) {
    }
}
