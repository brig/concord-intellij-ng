// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model.call;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.DynamicMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.psi.PsiElement;

import brig.concord.documentation.Documented;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class CallInParamsMetaType extends YamlAnyOfType implements DynamicMetaType {

    private static final CallInParamsMetaType INSTANCE = new CallInParamsMetaType();

    public static CallInParamsMetaType getInstance() {
        return INSTANCE;
    }

    private CallInParamsMetaType() {
        this(objectMetaType(null));
    }

    private CallInParamsMetaType(YamlMetaType objectType) {
        super(List.of(ExpressionMetaType.getInstance(), objectType),
                descKey("doc.step.feature.in.description"));
    }

    @Override
    public @NotNull List<Documented.DocumentedField> getDocumentationFields() {
        return streamSubTypes()
                .flatMap(t -> t.getDocumentationFields().stream())
                .toList();
    }

    @Override
    public YamlMetaType resolve(PsiElement element) {
        return new CallInParamsMetaType(objectMetaType(element));
    }

    private static YamlMetaType objectMetaType(PsiElement element) {
        return FlowCallParamsProvider.getInstance().inParams(element);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getTypeName() + " @ " + getSubTypes();
    }
}
