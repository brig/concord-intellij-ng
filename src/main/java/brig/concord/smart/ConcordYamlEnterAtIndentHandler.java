package brig.concord.smart;

import brig.concord.documentation.FlowDefinitionDocumentationParser;
import brig.concord.psi.CommentsProcessor;
import brig.concord.psi.ConcordFile;
import com.google.common.base.CharMatcher;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class ConcordYamlEnterAtIndentHandler extends EnterHandlerDelegateAdapter {

    @Override
    public Result preprocessEnter(@NotNull PsiFile file,
                                  @NotNull Editor editor,
                                  @NotNull Ref<Integer> caretOffset,
                                  @NotNull Ref<Integer> caretAdvance,
                                  @NotNull DataContext dataContext,
                                  EditorActionHandler originalHandler) {

        return Result.Continue;
    }

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        if (!(file instanceof ConcordFile) || !file.isValid()) {
            return Result.Continue;
        }

        int caretOffset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(caretOffset - 1);
        if (element == null) {
            return Result.Continue;
        }

        if (!isInsideCommentBlock(element)) {
            return Result.Continue;
        }

        int indentSize = getIndentInComment(element);
        editor.getDocument().insertString(caretOffset, "#" + StringUtil.repeat(" ", indentSize));
        editor.getCaretModel().moveToOffset(caretOffset + 1 + indentSize);

        return Result.Continue;
    }

    private static boolean isInsideCommentBlock(PsiElement element) {
        return CommentsProcessor.stream(element).filter(c -> "##".equals(c.getText())).count() == 1;
    }

    private static int getIndentInComment(PsiElement element) {
        PsiComment prevLine = CommentsProcessor.stream(element).findFirst().orElse(null);
        if (prevLine == null) {
            return 1;
        }

        String text = prevLine.getText();
        if (FlowDefinitionDocumentationParser.isHeader(text)) {
            return 1;
        }

        String clean = CharMatcher.anyOf("#").trimLeadingFrom(prevLine.getText());
        String trimmed = clean.trim();
        if (FlowDefinitionDocumentationParser.isIn(trimmed) || FlowDefinitionDocumentationParser.isOut(trimmed)) {
            return 2;
        }

        return countLeadingSpaces(clean);
    }

    private static int countLeadingSpaces(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
