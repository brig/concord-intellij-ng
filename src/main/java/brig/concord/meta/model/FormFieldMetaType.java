package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLBundle;
import org.jetbrains.yaml.meta.model.*;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormFieldMetaType extends ConcordMetaType {

    private static final FormFieldMetaType INSTANCE = new FormFieldMetaType();

    public static FormFieldMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("type");
    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "label", YamlStringType::getInstance,
            "type", FieldType::getInstance,
            "value", YamlAnything::getInstance,
            "allow", YamlAnything::getInstance
    );

    private static final Map<String, Supplier<YamlMetaType>> stringFeatures = Map.of(
            "pattern", RegexpMetaType::getInstance,
            "inputType", YamlStringType::getInstance,
            "placeholder", YamlStringType::getInstance,
            "search", YamlBooleanType::getSharedInstance,
            "readOnly", YamlBooleanType::getSharedInstance
    );

    private static final Map<String, Supplier<YamlMetaType>> intFeatures = Map.of(
            "min", IntegerMetaType::getInstance,
            "max", IntegerMetaType::getInstance,
            "placeholder", YamlStringType::getInstance,
            "readOnly", YamlBooleanType::getSharedInstance
    );

    private static final Map<String, Supplier<YamlMetaType>> booleanFeatures = Map.of(
            "readOnly", YamlBooleanType::getSharedInstance
    );

    private static final Map<String, Supplier<YamlMetaType>> dateFeatures = Map.of(
            "popupPosition", YamlStringType::getInstance,
            "readOnly", YamlBooleanType::getSharedInstance
    );

    private static final Map<String, Map<String, Supplier<YamlMetaType>>> featuresByType = Map.of(
            "string", stringFeatures,
            "int", intFeatures,
            "decimal", intFeatures,
            "boolean", booleanFeatures,
            "date", dateFeatures,
            "datetime", dateFeatures
    );

    private static final Map<String, Supplier<YamlMetaType>> allFeatures =
            Stream.of(features, stringFeatures, intFeatures, booleanFeatures, dateFeatures)
                    .flatMap(m -> m.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1));


    protected FormFieldMetaType() {
        super("Form field");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return allFeatures;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }
    @Override
    public @NotNull List<Field> computeKeyCompletions(YAMLMapping existingMapping) {
        Map<String, Supplier<YamlMetaType>> typeFeatures = Map.of();
        Map<String, Supplier<YamlMetaType>> features = FormFieldMetaType.features;
        if (existingMapping != null) {
            String type = getType(existingMapping);
            if (type != null) {
                typeFeatures = featuresByType.getOrDefault(type, Map.of());
            }
        }

        return Stream.concat(features.entrySet().stream(), typeFeatures.entrySet().stream())
                .map(e -> new Field(e.getKey(), e.getValue().get()))
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

        Map<String, Supplier<YamlMetaType>> typedFeatures = featuresByType.getOrDefault(type, Map.of());
        m.getKeyValues().stream()
                .map(YAMLKeyValue::getKeyText)
                .filter(k -> !features.containsKey(k))
                .filter(k -> !typedFeatures.containsKey(k))
                .forEach(k -> {
                    String msg = YAMLBundle.message("YamlUnknownKeysInspectionBase.unknown.key", k);
                    problemsHolder.registerProblem(Objects.requireNonNull(m.getKeyValueByKey(k)), msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                });
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
    }
}
