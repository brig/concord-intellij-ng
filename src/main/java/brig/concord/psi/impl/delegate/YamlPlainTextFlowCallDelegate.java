// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi.impl.delegate;

import brig.concord.psi.ref.FlowDefinitionReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLElementGenerator;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.impl.YAMLPlainTextImpl;

public class YamlPlainTextFlowCallDelegate extends YamlPlainTextDelegateAbstract implements PsiNamedElement {

    private final transient YAMLScalar flow;

    public YamlPlainTextFlowCallDelegate(YAMLPlainTextImpl flow) {
        super(flow.getNode());
        this.flow = flow;
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        YAMLKeyValue newValue = YAMLElementGenerator.getInstance(flow.getProject())
                .createYamlKeyValue("foo", newName);
        return replace(newValue);
    }

    @Override
    public PsiReference getReference() {
        return new FlowDefinitionReference(flow);
    }
}
