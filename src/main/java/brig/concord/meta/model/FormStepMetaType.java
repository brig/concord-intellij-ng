package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.BooleanMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.meta.model.value.MapMetaType;
import brig.concord.meta.model.value.StringMetaType;

import brig.concord.documentation.Documented;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.*;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FormStepMetaType extends IdentityMetaType {

    private static final FormStepMetaType INSTANCE = new FormStepMetaType();

    public static FormStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "form", new StringMetaType().withDescriptionKey("doc.step.form.key.description"),
            "yield", new BooleanMetaType().withDescriptionKey("doc.step.feature.yield.description"),
            "saveSubmittedBy", new BooleanMetaType().withDescriptionKey("doc.step.feature.saveSubmittedBy.description"),
            "runAs", new AnyMapMetaType().withDescriptionKey("doc.step.feature.runAs.description"),
            "values", new AnyMapMetaType().withDescriptionKey("doc.step.feature.values.description"),
            "fields", FieldsType.getInstance()
    );

    protected FormStepMetaType() {
        super("form", Set.of("form"));

        setDescriptionKey("doc.step.form.description");
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
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
            super(ExpressionMetaType.getInstance(), new YamlArrayType(FieldWrapper.getInstance()));

            setDescriptionKey("doc.step.feature.fields.description");
        }

        @Override
        public @NotNull List<Documented.DocumentedField> getDocumentationFields() {
            return FormFieldMetaType.getInstance().getDocumentationFields();
        }
    }
}
