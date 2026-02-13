package brig.concord.meta.model.call;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.DynamicMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;
import com.intellij.psi.PsiElement;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.List;

public class CallInParamsMetaType extends YamlAnyOfType implements DynamicMetaType {

    private static final CallInParamsMetaType INSTANCE = new CallInParamsMetaType();

    public static CallInParamsMetaType getInstance() {
        return INSTANCE;
    }

    private CallInParamsMetaType() {
        this(objectMetaType(null));
    }

    private CallInParamsMetaType(YamlMetaType objectType) {
        super(ExpressionMetaType.getInstance(), objectType);
        setDescriptionKey("doc.step.feature.in.description");
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
