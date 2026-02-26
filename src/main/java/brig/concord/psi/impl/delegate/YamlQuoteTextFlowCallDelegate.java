// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi.impl.delegate;

import brig.concord.psi.ref.FlowDefinitionReference;
import brig.concord.yaml.psi.impl.YAMLQuotedTextImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLElementGenerator;
import brig.concord.yaml.psi.YAMLScalar;

public class YamlQuoteTextFlowCallDelegate extends YamlQuoteTextDelegateAbstract implements PsiNamedElement {

    private final YAMLScalar flow;

    public YamlQuoteTextFlowCallDelegate(YAMLQuotedTextImpl flow) {
        super(flow.getNode());
        this.flow = flow;
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        String quoted = isSingleQuote()
                ? "'" + newName + "'"
                : "\"" + newName + "\"";
        YAMLScalar newValue = YAMLElementGenerator.getInstance(flow.getProject())
                .createYamlScalar(quoted);
        return replace(newValue);
    }

    @Override
    public PsiReference getReference() {
        return new FlowDefinitionReference(flow);
    }
}
