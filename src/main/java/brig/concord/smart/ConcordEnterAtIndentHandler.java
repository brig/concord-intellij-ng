package brig.concord.smart;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.FlowDocumentation;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class ConcordEnterAtIndentHandler implements EnterHandlerDelegate {

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
        if (!isValidConcordFile(file)) {
            return Result.Continue;
        }

        int caretOffset = editor.getCaretModel().getOffset();
        if (caretOffset <= 0) {
            return Result.Continue;
        }

        if (handleFlowDocumentation(file, editor, caretOffset)) {
            return Result.Stop;
        }

        if (handleBasicIndentation(editor, caretOffset)) {
            return Result.Stop;
        }

        return Result.Continue;
    }

    private boolean isValidConcordFile(PsiFile file) {
        return file instanceof ConcordFile && file.isValid();
    }

    private boolean handleFlowDocumentation(PsiFile file, Editor editor, int caretOffset) {
        PsiElement element = file.findElementAt(caretOffset - 1);
        if (element != null && isInsideFlowDocumentation(element)) {
            int indentSize = getIndentSize(editor, caretOffset);
            String textToInsert = "#" + StringUtil.repeat(" ", indentSize);
            editor.getDocument().insertString(caretOffset, textToInsert);
            editor.getCaretModel().moveToOffset(caretOffset + textToInsert.length());
            return true;
        }
        return false;
    }

    private boolean handleBasicIndentation(Editor editor, int caretOffset) {
        Document document = editor.getDocument();
        int lineNumber = document.getLineNumber(caretOffset);
        int lineStart = document.getLineStartOffset(lineNumber);

        if (caretOffset == lineStart) {
            String prevLine = getPreviousLine(document, lineNumber);
            if (prevLine != null) {
                String indent = getLeadingWhitespace(prevLine);
                if (!indent.isEmpty()) {
                    document.insertString(caretOffset, indent);
                    editor.getCaretModel().moveToOffset(caretOffset + indent.length());
                    return true;
                }
            }
        }
        return false;
    }

    private static String getPreviousLine(Document document, int currentLineNumber) {
        int prevLineNumber = currentLineNumber - 1;
        if (prevLineNumber >= 0) {
            int prevLineStart = document.getLineStartOffset(prevLineNumber);
            int prevLineEnd = document.getLineEndOffset(prevLineNumber);
            return document.getText().substring(prevLineStart, prevLineEnd);
        }
        return null;
    }

    private static String getLeadingWhitespace(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                count++;
            } else {
                break;
            }
        }
        return str.substring(0, count);
    }

    private static boolean isInsideFlowDocumentation(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, FlowDocumentation.class) != null;
    }

    private static int getIndentSize(Editor editor, int caretOffset) {
        int lineNumber = editor.getDocument().getLineNumber(caretOffset);
        String prevLine = getPreviousLine(editor.getDocument(), lineNumber);
        
        if (prevLine == null) {
            return 1;
        }

        String trimmed = prevLine.trim();
        if (trimmed.equals("##")) {
            return 1;
        }

        // Check for section headers (in: or out:)
        String afterHash = trimmed.startsWith("#") ? trimmed.substring(1).trim() : trimmed;
        if (StringUtil.startsWithIgnoreCase(afterHash, "in:") || StringUtil.startsWithIgnoreCase(afterHash, "out:")) {
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