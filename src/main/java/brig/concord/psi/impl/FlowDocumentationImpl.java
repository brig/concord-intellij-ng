package brig.concord.psi.impl;

import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static brig.concord.lexer.FlowDocTokenTypes.FLOW_DOC_CONTENT;

public class FlowDocumentationImpl extends ASTWrapperPsiElement implements FlowDocumentation {

    public FlowDocumentationImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public String getDescription() {
        var descElement = findChildByType(FlowDocElementTypes.FLOW_DOC_DESCRIPTION);
        if (descElement == null) {
            return null;
        }

        var description = new StringBuilder();
        var contentNodes = descElement.getNode().getChildren(null);

        for (var node : contentNodes) {
            if (node.getElementType() == FLOW_DOC_CONTENT) {
                var text = node.getText().trim();
                if (text.isEmpty() || text.equals("#")) {
                    continue;
                }
                if (!description.isEmpty()) {
                    description.append("\n");
                }
                description.append(text);
            }
        }

        return !description.isEmpty() ? description.toString() : null;
    }

    @Override
    @NotNull
    public List<FlowDocParameter> getInputParameters() {
        var inSection = findChildByType(FlowDocElementTypes.FLOW_DOC_IN_SECTION);
        return getFlowDocParameters(inSection);
    }

    @Override
    @NotNull
    public List<FlowDocParameter> getOutputParameters() {
        var outSection = findChildByType(FlowDocElementTypes.FLOW_DOC_OUT_SECTION);
        return getFlowDocParameters(outSection);
    }

    @Override
    @Nullable
    public YAMLKeyValue getDocumentedFlow() {
        // Flow documentation should be immediately before the flow key-value
        // We need to find the next sibling that is a YAMLKeyValue
        var sibling = getNextSibling();
        while (sibling != null) {
            if (sibling instanceof YAMLKeyValue) {
                return (YAMLKeyValue) sibling;
            }
            sibling = sibling.getNextSibling();
        }
        return null;
    }

    @Override
    @Nullable
    public String getFlowName() {
        var flow = getDocumentedFlow();
        return flow != null ? flow.getKeyText() : null;
    }

    @Override
    @Nullable
    public FlowDocParameter findParameter(@NotNull String parameterName) {
        for (var param : getInputParameters()) {
            if (parameterName.equals(param.getName())) {
                return param;
            }
        }

        for (var param : getOutputParameters()) {
            if (parameterName.equals(param.getName())) {
                return param;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        var flowName = getFlowName();
        return "FlowDocumentation" + (flowName != null ? " for " + flowName : "");
    }

    @NotNull
    private static List<FlowDocParameter> getFlowDocParameters(@Nullable PsiElement inSection) {
        if (inSection == null) {
            return List.of();
        }

        var params = new ArrayList<FlowDocParameter>();
        var children = inSection.getChildren();
        for (var child : children) {
            if (child instanceof FlowDocParameter) {
                params.add((FlowDocParameter) child);
            }
        }

        return params;
    }
}
