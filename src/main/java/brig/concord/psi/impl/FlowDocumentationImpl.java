package brig.concord.psi.impl;

import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        var description = Arrays.stream(descElement.getNode().getChildren(null))
                .filter(node -> node.getElementType() == FLOW_DOC_CONTENT)
                .map(node -> node.getText().trim())
                .filter(text -> !text.isEmpty() && !text.equals("#"))
                .collect(Collectors.joining("\n"));

        return !description.isEmpty() ? description : null;
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
        return PsiTreeUtil.getNextSiblingOfType(this, YAMLKeyValue.class);
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
        for (FlowDocParameter param : getInputParameters()) {
            if (parameterName.equals(param.getName())) {
                return param;
            }
        }
        for (FlowDocParameter param : getOutputParameters()) {
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

        int insertOffset;
        String text;

        if (inSection != null) {
            var params = getInputParameters();
            if (params.isEmpty()) {
                // Insert after "in:" on the same line
                var sectionStart = inSection.getTextRange().getStartOffset();
                var lineNum = document.getLineNumber(sectionStart);
                insertOffset = document.getLineEndOffset(lineNum);

                var sectionPrefix = getLinePrefix(sectionStart, document);
                text = "\n" + sectionPrefix + "  " + name + ": " + type;
            } else {
                var lastParam = params.getLast();
                insertOffset = lastParam.getTextRange().getEndOffset();

                var paramPrefix = getLinePrefix(lastParam.getTextRange().getStartOffset(), document);
                text = "\n" + paramPrefix + name + ": " + type;
            }
        } else {
            var outSection = findChildByType(FlowDocElementTypes.FLOW_DOC_OUT_SECTION);
            if (outSection != null) {
                insertOffset = getLineStartOffset(outSection.getTextRange().getStartOffset(), document);
            } else {
                // Find closing ## marker
                insertOffset = getLineStartOffset(getTextRange().getEndOffset() - 1, document);
            }

            text = indent + "# in:\n" + indent + "#   " + name + ": " + type + "\n";
        }

        document.insertString(insertOffset, text);
        PsiDocumentManager.getInstance(getProject()).commitDocument(document);
    }

    private String detectIndent() {
        var indent = YAMLUtil.getIndentInThisLine(this);
        return StringUtil.repeat(" ", indent);
    }

    private static String getLinePrefix(int offset, Document document) {
        var lineNum = document.getLineNumber(offset);
        var lineStart = document.getLineStartOffset(lineNum);
        return document.getText().substring(lineStart, offset);
    }

    private static int getLineStartOffset(int offset, Document document) {
        var lineNum = document.getLineNumber(offset);
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
