package brig.concord.psi.impl.delegate;

import brig.concord.psi.ref.FlowDefinitionReference;
import com.intellij.psi.PsiReference;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class YamlFlowCallDelegate extends YamlKeyValueDelegateAbstract {

    private final transient YAMLKeyValue keyValue;

    public YamlFlowCallDelegate(YAMLKeyValue keyValue) {
        super(keyValue.getNode());
        this.keyValue = keyValue;
    }

    @Override
    public PsiReference getReference() {
        if (getValue() == null) {
            return null;
        }

        return new FlowDefinitionReference(keyValue, getValue().getTextRangeInParent());
    }
}
