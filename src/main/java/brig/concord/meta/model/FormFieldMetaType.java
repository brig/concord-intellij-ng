package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.AnythingMetaType;
import brig.concord.meta.model.value.BooleanMetaType;
import brig.concord.meta.model.value.IntegerMetaType;
import brig.concord.meta.model.value.RegexpMetaType;
import brig.concord.meta.model.value.StringMetaType;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormFieldMetaType extends ConcordMetaType implements HighlightProvider {

    private static final FormFieldMetaType INSTANCE = new FormFieldMetaType();

    public static FormFieldMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("type");
    private static final Map<String, YamlMetaType> features = caseInsensitiveMap(Map.of(
            "label", StringMetaType.getInstance(),
            "type", FieldType.getInstance(),
            "value", AnythingMetaType.getInstance(),
            "allow", AnythingMetaType.getInstance()
    ));

    private static final Map<String, YamlMetaType> stringFeatures = caseInsensitiveMap(Map.of(
            "pattern", RegexpMetaType.getInstance(),
            "inputType", StringMetaType.getInstance(),
            "placeholder", StringMetaType.getInstance(),
            "search", BooleanMetaType.getInstance(),
            "readOnly", BooleanMetaType.getInstance()
    ));

    private static final Map<String, YamlMetaType> intFeatures = Map.of(
            "min", IntegerMetaType.getInstance(),
            "max", IntegerMetaType.getInstance(),
            "placeholder", StringMetaType.getInstance(),
            "readOnly", BooleanMetaType.getInstance()
    );

    private static final Map<String, YamlMetaType> booleanFeatures = Map.of(
            "readOnly", BooleanMetaType.getInstance()
    );

    private static final Map<String, YamlMetaType> dateFeatures = Map.of(
            "popupPosition", StringMetaType.getInstance(),
            "readOnly", BooleanMetaType.getInstance()
    );

    private static final Map<String, Map<String, YamlMetaType>> featuresByType = Map.of(
            "string", stringFeatures,
            "int", intFeatures,
            "decimal", intFeatures,
            "boolean", booleanFeatures,
            "date", dateFeatures,
            "datetime", dateFeatures
    );

    private static final Map<String, YamlMetaType> allFeatures =
            Stream.of(features, stringFeatures, intFeatures, booleanFeatures, dateFeatures)
                    .flatMap(m -> m.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));


    protected FormFieldMetaType() {
        setDocBundlePrefix("doc.forms.formName.formField");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return allFeatures;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(YAMLMapping existingMapping) {
        Map<String, YamlMetaType> typeFeatures = Map.of();
        Map<String, YamlMetaType> features = FormFieldMetaType.features;
        if (existingMapping != null) {
            String type = getType(existingMapping);
            if (type != null) {
                typeFeatures = featuresByType.getOrDefault(type, Map.of());
            }
        }

        return Stream.concat(features.entrySet().stream(), typeFeatures.entrySet().stream())
                .map(e -> new Field(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(Field::getName))
                .collect(Collectors.toList());
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (!(value instanceof YAMLMapping m)) {
            return;
        }

        String type = getType(m);
        if (type == null) {
            return;
        }

        if (!FieldType.isValidType(type)) {
            return;
        }

        Map<String, YamlMetaType> typedFeatures = featuresByType.getOrDefault(type, Map.of());
        m.getKeyValues().stream()
                .map(YAMLKeyValue::getKeyText)
                .filter(allFeatures::containsKey) // unknown key is not our business
                .filter(k -> !features.containsKey(k))
                .filter(k -> !typedFeatures.containsKey(k))
                .map(m::getKeyValueByKey)
                .filter(Objects::nonNull)
                .map(YAMLKeyValue::getKey)
                .filter(Objects::nonNull)
                .forEach(k -> {
                    String msg = ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", k.getText());
                    problemsHolder.registerProblem(k, msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                });
    }

    @Override
    public @Nullable String getDocumentationBody(@NotNull PsiElement element) {
        if (!(element instanceof YAMLKeyValue kv)) {
            return null;
        }

        var fieldName = kv.getKeyText();
        var formName = findFormName(kv);

        var prefix = getDocBundlePrefix();
        var sb = new StringBuilder();

        sb.append("<p>Type: <code>object</code></p>");

        sb.append("<p>Form field definition. Submitted value is stored in <code>");
        if (formName != null) {
            sb.append(formName).append(".").append(fieldName);
        } else {
            sb.append("formName.").append(fieldName);
        }
        sb.append("</code>.</p>");

        // Common keys
        sb.append("<p><b>Keys:</b></p>");
        sb.append("<ul>");
        appendKeyItem(sb, "type", "string", ConcordBundle.findMessage(prefix + ".type.description"));
        appendKeyItem(sb, "label", "string", ConcordBundle.findMessage(prefix + ".label.description"));
        appendKeyItem(sb, "value", "any", ConcordBundle.findMessage(prefix + ".value.description"));
        appendKeyItem(sb, "allow", "any", ConcordBundle.findMessage(prefix + ".allow.description"));
        sb.append("</ul>");

        // Keys by type
        sb.append("<p><b>Keys by type:</b></p>");

        appendTypeSection(sb, "string", List.of(
                entry("pattern", "string", "regular expression to validate the value"),
                entry("inputType", "string", "HTML input type (for example: <code>text</code>, <code>password</code>, <code>email</code>)"),
                entry("readonly", "boolean", "make the field read-only"),
                entry("placeholder", "string", "short hint shown in the input"),
                entry("search", "boolean", "allow searching in dropdown input")
        ));

        appendTypeSection(sb, "int", List.of(
                entry("min", "integer", "minimum value"),
                entry("max", "integer", "maximum value"),
                entry("readonly", "boolean", "make the field read-only"),
                entry("placeholder", "string", "short hint shown in the input")
        ));

        appendTypeSection(sb, "decimal", List.of(
                entry("min", "integer", "minimum value"),
                entry("max", "integer", "maximum value"),
                entry("readonly", "boolean", "make the field read-only"),
                entry("placeholder", "string", "short hint shown in the input")
        ));

        appendTypeSection(sb, "boolean", List.<String[]>of(
                entry("readonly", "boolean", "make the field read-only")
        ));

        appendTypeSection(sb, "file", List.<String[]>of(
                entry("type", "file", "file upload field; uploaded file is stored in the process workspace")
        ));

        sb.append("<p><b>Cardinality:</b> <code>string</code> single, <code>string?</code> optional, <code>string+</code> one or more, <code>string*</code> zero or more</p>");

        return sb.toString();
    }

    private static void appendKeyItem(StringBuilder sb, String name, String type, @Nullable String description) {
        sb.append("<li><code>").append(name).append("</code>");
        sb.append(" <i>(").append(type).append(")</i>");
        if (description != null) {
            sb.append(" &mdash; ").append(com.intellij.openapi.util.text.StringUtil.decapitalize(description));
        }
        sb.append("</li>");
    }

    private static void appendTypeSection(StringBuilder sb, String typeName, List<String[]> entries) {
        sb.append("<p><b>").append(typeName).append("</b></p>");
        sb.append("<ul>");
        for (var e : entries) {
            sb.append("<li><code>").append(e[0]).append("</code>");
            sb.append(" <i>(").append(e[1]).append(")</i>");
            sb.append(" &mdash; ").append(e[2]);
            sb.append("</li>");
        }
        sb.append("</ul>");
    }

    private static String[] entry(String name, String type, String description) {
        return new String[]{name, type, description};
    }

    private static @Nullable String findFormName(@NotNull YAMLKeyValue kv) {
        // Navigate: fieldKV -> YAMLMapping -> YAMLSequenceItem -> YAMLSequence -> YAMLKeyValue(formName)
        var parent = kv.getParent();
        if (parent != null) {
            parent = parent.getParent();
        }
        if (parent != null) {
            parent = parent.getParent();
        }
        if (parent != null && parent.getParent() instanceof YAMLKeyValue formKv) {
            return formKv.getKeyText();
        }
        return null;
    }

    private static String getType(YAMLMapping m) {
        return Optional.ofNullable(m.getKeyValueByKey("type"))
                .map(YAMLKeyValue::getValueText)
                .map(FormFieldMetaType::normalizeType)
                .orElse(null);
    }

    private static String normalizeType(String typeWithCardinality) {
        if (typeWithCardinality.endsWith("?")
                || typeWithCardinality.endsWith("+")
                || typeWithCardinality.endsWith("*")) {
            return typeWithCardinality.substring(0, typeWithCardinality.length() - 1);
        }
        return typeWithCardinality;
    }

    private static Map<String, YamlMetaType> caseInsensitiveMap(Map<String, YamlMetaType> m) {
        Map<String, YamlMetaType> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(m);
        return result;
    }

    private static class FieldType extends YamlEnumType {

        private static final Set<String> types = Set.of("string", "int", "decimal", "boolean", "file", "date", "dateTime");
        private static final Set<String> cardinality = Set.of("?", "+", "*");

        private static final FieldType INSTANCE = new FieldType();

        public static FieldType getInstance() {
            return INSTANCE;
        }

        public FieldType() {
            super("form field type");

            List<String> literals = new ArrayList<>();
            for (String t : types) {
                literals.add(t);
                for (String c : cardinality) {
                    literals.add(t + c);
                }
            }
            withLiterals(literals.toArray(new String[0]));
        }

        public static boolean isValidType(String type) {
            return types.contains(type);
        }
    }
}
