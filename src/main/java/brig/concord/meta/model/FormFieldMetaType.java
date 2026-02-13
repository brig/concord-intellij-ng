package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.*;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class FormFieldMetaType extends ConcordMetaType implements HighlightProvider {

    private static final FormFieldMetaType INSTANCE = new FormFieldMetaType();

    public static FormFieldMetaType getInstance() {
        return INSTANCE;
    }

    private static final List<String> featureKeys = List.of("type", "label", "value", "allow");
    private static final Map<String, YamlMetaType> features = caseInsensitiveMap(Map.of(
            "type", FieldType.getInstance(),
            "label", new StringMetaType(descKey("doc.forms.formName.formField.label.description")),
            "value", new AnythingMetaType(descKey("doc.forms.formName.formField.value.description")),
            "allow", new AnythingMetaType(descKey("doc.forms.formName.formField.allow.description"))
    ));

    private static final Map<String, YamlMetaType> stringFeatures = caseInsensitiveMap(Map.of(
            "pattern", new RegexpMetaType(descKey("doc.forms.formName.formField.pattern.description")),
            "inputType", new StringMetaType(descKey("doc.forms.formName.formField.inputType.description")),
            "placeholder", new StringMetaType(descKey("doc.forms.formName.formField.placeholder.description")),
            "search", new BooleanMetaType(descKey("doc.forms.formName.formField.search.description")),
            "readOnly", new BooleanMetaType(descKey("doc.forms.formName.formField.readonly.description"))
    ));

    private static final Map<String, YamlMetaType> intFeatures = Map.of(
            "min", new IntegerMetaType(descKey("doc.forms.formName.formField.min.description")),
            "max", new IntegerMetaType(descKey("doc.forms.formName.formField.max.description")),
            "placeholder", new StringMetaType(descKey("doc.forms.formName.formField.placeholder.description")),
            "readOnly", new BooleanMetaType(descKey("doc.forms.formName.formField.readonly.description"))
    );

    private static final Map<String, YamlMetaType> booleanFeatures = Map.of(
            "readOnly", new BooleanMetaType(descKey("doc.forms.formName.formField.readonly.description"))
    );

    private static final Map<String, YamlMetaType> dateFeatures = Map.of(
            "popupPosition", new StringMetaType(descKey("doc.forms.formName.formField.popupPosition.description")),
            "readOnly", new BooleanMetaType(descKey("doc.forms.formName.formField.readonly.description"))
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


    private FormFieldMetaType() {
        super(descKey("doc.forms.formName.formField.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return allFeatures;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(YAMLMapping existingMapping) {
        Map<String, YamlMetaType> typeFeatures = Map.of();
        var features = FormFieldMetaType.features;
        if (existingMapping != null) {
            var type = getType(existingMapping);
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

        var type = getType(m);
        if (type == null) {
            return;
        }

        if (!FieldType.isValidType(type)) {
            return;
        }

        var typedFeatures = featuresByType.getOrDefault(type, Map.of());
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
                    var msg = ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", k.getText());
                    problemsHolder.registerProblem(k, msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                });
    }

    @Override
    public @Nullable String getDocumentationDescription(@NotNull YAMLKeyValue element) {
        var fieldName = element.getKeyText();
        var formName = findFormName(element);
        var qualifiedName = (formName != null ? formName : "formName") + "." + fieldName;
        return ConcordBundle.message("doc.forms.formName.formField.description.resolved", qualifiedName);
    }

    @Override
    public @NotNull List<DocumentedField> getDocumentationFields() {
        return featureKeys.stream()
                .map(key -> {
                    var mt = features.get(key);
                    return new DocumentedField(key, mt.getTypeName(), false, mt.getDescription(), List.of());
                })
                .toList();
    }

    /**
     * Returns documentation fields for all features (common + type-specific).
     * Used by FormStepMetaType.FieldsType to show the full set of available keys.
     */
    public @NotNull List<DocumentedField> getAllDocumentationFields() {
        return super.getDocumentationFields();
    }

    @Override
    public @NotNull List<DocumentedSection> getDocumentationSections() {
        var sections = new ArrayList<DocumentedSection>();
        sections.add(new DocumentedSection("Keys by type:", List.of()));
        sections.add(buildTypeSection("string", stringFeatures));
        sections.add(buildTypeSection("int", intFeatures));
        sections.add(buildTypeSection("decimal", intFeatures));
        sections.add(buildTypeSection("boolean", booleanFeatures));
        sections.add(new DocumentedSection("file", List.of(
                new DocumentedField("type", "file", false,
                        ConcordBundle.message("doc.forms.formName.formField.file.description"), List.of())
        )));
        return sections;
    }

    @Override
    public @Nullable String getDocumentationFooter() {
        return ConcordBundle.message("doc.forms.formName.formField.cardinality");
    }

    private static DocumentedSection buildTypeSection(String typeName, Map<String, YamlMetaType> typeFeatures) {
        var fields = typeFeatures.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(e -> new DocumentedField(
                        e.getKey(), e.getValue().getTypeName(), false,
                        e.getValue().getDescription(), List.of()))
                .toList();
        return new DocumentedSection(typeName, fields);
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
            super("string", descKey("doc.forms.formName.formField.type.description").andRequired());

            List<String> literals = new ArrayList<>();
            for (var t : types) {
                literals.add(t);
                for (var c : cardinality) {
                    literals.add(t + c);
                }
            }
            setLiterals(literals.toArray(new String[0]));
        }

        public static boolean isValidType(String type) {
            return types.contains(type);
        }
    }
}
