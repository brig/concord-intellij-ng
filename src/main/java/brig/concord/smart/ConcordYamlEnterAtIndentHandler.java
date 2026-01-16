package brig.concord.smart;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.FlowDocumentation;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
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

        if (!isInsideFlowDocumentation(element)) {
            return Result.Continue;
        }

        int indentSize = getIndentSize(editor, caretOffset);
        editor.getDocument().insertString(caretOffset, "#" + StringUtil.repeat(" ", indentSize));
        editor.getCaretModel().moveToOffset(caretOffset + 1 + indentSize);

        return Result.Continue;
    }

    private static boolean isInsideFlowDocumentation(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, FlowDocumentation.class) != null;
    }

    private static int getIndentSize(Editor editor, int caretOffset) {
        // Get the previous line to determine indent
        int lineNumber = editor.getDocument().getLineNumber(caretOffset);
        if (lineNumber == 0) {
            return 1;
        }

        int prevLineStart = editor.getDocument().getLineStartOffset(lineNumber - 1);
        int prevLineEnd = editor.getDocument().getLineEndOffset(lineNumber - 1);
        String prevLine = editor.getDocument().getText().substring(prevLineStart, prevLineEnd);

        String trimmed = prevLine.trim();
        if (trimmed.equals("##")) {
            return 1;
        }

        // Check for section headers (in: or out:)
        String afterHash = trimmed.startsWith("#") ? trimmed.substring(1).trim() : trimmed;
        if (afterHash.toLowerCase().startsWith("in:") || afterHash.toLowerCase().startsWith("out:")) {
            return 3; // indent for parameters
        }

        // Count leading spaces after #
        int hashIndex = prevLine.indexOf('#');
        if (hashIndex >= 0 && hashIndex + 1 < prevLine.length()) {
            return countLeadingSpaces(prevLine.substring(hashIndex + 1));
        }

        return 1;
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
        return Math.max(1, count);
    }
}