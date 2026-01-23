package brig.concord.psi.impl;

import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        var flowName = ReadAction.compute(this::getFlowName);
        return "FlowDocumentation" + (flowName != null ? " for " + flowName : "");
    }

    @Override
    public void addInputParameter(@NotNull String name, @NotNull String type) {
        var document = getContainingFile().getViewProvider().getDocument();
        if (document == null) {
            return;
        }

        var indent = detectIndent();

        var inSection = findChildByType(FlowDocElementTypes.FLOW_DOC_IN_SECTION);
        if (inSection != null) {
            // Add parameter to existing in: section
            int insertOffset;
            String text;
            var params = getInputParameters();
            if (params.isEmpty()) {
                // Insert after "in:" on the same line - find end of "in:" text
                int sectionStart = inSection.getTextRange().getStartOffset();
                int lineNum = document.getLineNumber(sectionStart);
                insertOffset = document.getLineEndOffset(lineNum);
                // Compute prefix based on in: section position + 2 spaces for param indent
                String sectionPrefix = getSectionLinePrefix(inSection, document);
                text = "\n" + sectionPrefix + "  " + name + ": " + type;
            } else {
                var lastParam = params.getLast();
                insertOffset = lastParam.getTextRange().getEndOffset();
                // Copy the prefix from the last parameter's line (indent + "# " + spaces)
                String paramPrefix = getParamLinePrefix(lastParam, document);
                text = "\n" + paramPrefix + name + ": " + type;
            }
            document.insertString(insertOffset, text);
        } else {
            // Create new in: section - find where to insert
            var outSection = findChildByType(FlowDocElementTypes.FLOW_DOC_OUT_SECTION);

            int insertOffset;
            if (outSection != null) {
                // Insert before out: section - find start of line with "# out:"
                insertOffset = findLineStartBefore(outSection);
            } else {
                // Insert before closing ## - find start of line with "##"
                insertOffset = findClosingMarkerLineStart();
            }

            var text = indent + "# in:\n" + indent + "#   " + name + ": " + type + "\n";
            document.insertString(insertOffset, text);
        }

        PsiDocumentManager.getInstance(getProject()).commitDocument(document);
    }

    private String detectIndent() {
        var indent = YAMLUtil.getIndentInThisLine(this);
        return StringUtil.repeat(" ", indent);
    }

    private String getParamLinePrefix(FlowDocParameter param, com.intellij.openapi.editor.Document document) {
        // Get the prefix of the parameter's line (from line start to parameter name start)
        // This includes indentation, "#", and any spaces before the parameter name
        int paramStart = param.getTextRange().getStartOffset();
        int lineNum = document.getLineNumber(paramStart);
        int lineStart = document.getLineStartOffset(lineNum);
        return document.getText().substring(lineStart, paramStart);
    }

    private String getSectionLinePrefix(PsiElement section, com.intellij.openapi.editor.Document document) {
        // Get the prefix of the section's line (from line start to section header start)
        // For "  #     in:" this returns "  #     "
        int sectionStart = section.getTextRange().getStartOffset();
        int lineNum = document.getLineNumber(sectionStart);
        int lineStart = document.getLineStartOffset(lineNum);
        return document.getText().substring(lineStart, sectionStart);
    }

    private int findLineStartBefore(PsiElement element) {
        // Find the start of line containing this element (including the # prefix)
        var document = getContainingFile().getViewProvider().getDocument();
        if (document == null) {
            return element.getTextRange().getStartOffset();
        }
        var lineNum = document.getLineNumber(element.getTextRange().getStartOffset());
        return document.getLineStartOffset(lineNum);
    }

    private int findClosingMarkerLineStart() {
        // Find the start of line with closing ##
        var document = getContainingFile().getViewProvider().getDocument();
        if (document == null) {
            return getTextRange().getEndOffset();
        }
        // Closing ## is at the end of this element
        var endOffset = getTextRange().getEndOffset();
        var lineNum = document.getLineNumber(endOffset - 1); // -1 to be inside ##
        return document.getLineStartOffset(lineNum);
    }

    @NotNull
    private static List<FlowDocParameter> getFlowDocParameters(@Nullable PsiElement inSection) {
        if (inSection == null) {
            return List.of();
        }

        return PsiTreeUtil.getChildrenOfTypeAsList(inSection, FlowDocParameter.class);
    }
}
