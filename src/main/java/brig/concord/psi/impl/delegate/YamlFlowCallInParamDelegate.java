package brig.concord.psi.impl.delegate;

import brig.concord.psi.ref.CallInParamDefinitionReference;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class YamlFlowCallInParamDelegate extends YamlKeyValueDelegateAbstract implements PsiNamedElement {

    private final transient YAMLKeyValue inParam;

    public YamlFlowCallInParamDelegate(YAMLKeyValue inParam) {
        super(inParam.getNode());
        this.inParam = inParam;
    }

    @Override
    public PsiReference getReference() {
        return new CallInParamDefinitionReference(inParam);
    }
}