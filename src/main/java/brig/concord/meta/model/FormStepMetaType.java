// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.documentation.Documented;
import brig.concord.meta.model.value.*;
import brig.concord.yaml.meta.model.CompletionContext;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class FormStepMetaType extends IdentityMetaType {

    private static final FormStepMetaType INSTANCE = new FormStepMetaType();

    public static FormStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "form", new StringMetaType(descKey("doc.step.form.key.description").andRequired()),
            "yield", new BooleanMetaType(descKey("doc.step.feature.yield.description")),
            "saveSubmittedBy", new BooleanMetaType(descKey("doc.step.feature.saveSubmittedBy.description")),
            "runAs", new AnyMapMetaType(descKey("doc.step.feature.runAs.description")),
            "values", new AnyMapMetaType(descKey("doc.step.feature.values.description")),
            "fields", FieldsType.getInstance()
    );

    private FormStepMetaType() {
        super("form", descKey("doc.step.form.description"));
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

        private FieldsType() {
            super(List.of(ExpressionMetaType.getInstance(), new YamlArrayType(FieldWrapper.getInstance())),
                    descKey("doc.step.feature.fields.description"));
        }

        @Override
        public @NotNull List<Documented.DocumentedField> getDocumentationFields() {
            return FormFieldMetaType.getInstance().getAllDocumentationFields();
        }
    }
}
