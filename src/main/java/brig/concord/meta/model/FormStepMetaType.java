package brig.concord.meta.model;

import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.*;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class FormStepMetaType extends IdentityMetaType {

    private static final FormStepMetaType INSTANCE = new FormStepMetaType();

    public static FormStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "form", StringMetaType::getInstance, // TODO: type for search
            "yield", BooleanMetaType::getInstance,
            "saveSubmittedBy", BooleanMetaType::getInstance,
            "runAs", AnyMapMetaType::getInstance,
            "values", AnyMapMetaType::getInstance,
            "fields", FieldsType::getInstance
    );

    protected FormStepMetaType() {
        super("Form", "form", Set.of("form"));
    }

    @Override
    public @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        return super.getValueLookups(insertedScalar, completionContext);
    }

    private static class FieldWrapper extends MapMetaType {

        private static final FieldWrapper INSTANCE = new FieldWrapper();

        public static FieldWrapper getInstance() {
            return INSTANCE;
        }

        protected FieldWrapper() {
            super("Form call field");
        }

        @Override
        protected YamlMetaType getMapEntryType(String name) {
            return FormFieldMetaType.getInstance();
        }
    }

    private static class FieldsType extends YamlAnyOfType {

        private static final FieldsType INSTANCE = new FieldsType();

        public static FieldsType getInstance() {
            return INSTANCE;
        }

        protected FieldsType() {
            super("Form call fields", List.of(ExpressionMetaType.getInstance(), new YamlArrayType(FieldWrapper.getInstance())));
        }
    }
}
