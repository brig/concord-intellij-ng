package brig.concord.completion;

import brig.concord.el.ElLexerAdapter;
import brig.concord.el.psi.ElTypes;
import brig.concord.psi.ElExpressionElementType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElVariableCompletionContributor extends CompletionContributor {

    private record Variable(String name, String type, String description) {}

    private static final List<Variable> BUILT_IN_VARIABLES = List.of(
            new Variable("context", "Context", "Process execution context"),
            new Variable("execution", "Context", "Alias for context"),
            new Variable("txId", "String", "Process instance ID (UUID)"),
            new Variable("workDir", "String", "Working directory path"),
            new Variable("initiator", "UserInfo", "User who started the process"),
            new Variable("currentUser", "UserInfo", "Current process user"),
            new Variable("requestInfo", "RequestInfo", "HTTP request info"),
            new Variable("projectInfo", "ProjectInfo", "Project metadata"),
            new Variable("processInfo", "ProcessInfo", "Process info"),
            new Variable("tasks", "Map", "Available task plugins"),
            new Variable("parentInstanceId", "String", "Parent process ID"),
            new Variable("currentFlowName", "String", "Current flow name")
    );

    public ElVariableCompletionContributor() {

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new VariableCompletionProvider());
    }

    private static @Nullable PsiElement findElExpressionAncestor(@NotNull PsiElement element) {
        var parent = element;
        while (parent != null) {
            if (parent.getNode().getElementType() == ElExpressionElementType.INSTANCE) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Find the EL token type immediately before the given offset within the expression text.
     */
    private static @Nullable IElementType findPrecedingElTokenType(@NotNull String exprText, int caretOffsetInExpr) {
        var lexer = new ElLexerAdapter();
        lexer.start(exprText);
        IElementType prevType = null;
        while (lexer.getTokenType() != null) {
            if (lexer.getTokenStart() >= caretOffsetInExpr) {
                break;
            }
            var tokenType = lexer.getTokenType();
            if (tokenType != TokenType.WHITE_SPACE) {
                prevType = tokenType;
            }
            lexer.advance();
        }
        return prevType;
    }

    /**
     * Extract the identifier prefix at the caret position within the expression text.
     */
    private static @NotNull String extractIdentifierPrefix(@NotNull String exprText, int caretOffsetInExpr) {
        int start = caretOffsetInExpr;
        while (start > 0) {
            char c = exprText.charAt(start - 1);
            if (Character.isLetterOrDigit(c) || c == '_') {
                start--;
            } else {
                break;
            }
        }
        if (start >= caretOffsetInExpr) {
            return "";
        }
        return exprText.substring(start, caretOffsetInExpr);
    }

    private static class VariableCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();
            var elExpr = findElExpressionAncestor(position);
            if (elExpr == null) {
                return;
            }

            var exprText = elExpr.getText();
            var caretOffset = parameters.getOffset();
            var exprStartOffset = elExpr.getTextRange().getStartOffset();
            var caretOffsetInExpr = caretOffset - exprStartOffset;

            if (caretOffsetInExpr < 0 || caretOffsetInExpr > exprText.length()) {
                return;
            }

            // Find the EL token preceding the caret
            var prevTokenType = findPrecedingElTokenType(exprText, caretOffsetInExpr);

            // Skip property access (caret is after a dot)
            if (prevTokenType == ElTypes.DOT) {
                return;
            }

            // Only complete after LBRACE (${) or after operators, not at start or after $
            if (prevTokenType == null) {
                return;
            }

            var prefix = extractIdentifierPrefix(exprText, caretOffsetInExpr);

            var prefixedResult = result.withPrefixMatcher(prefix);
            for (var variable : BUILT_IN_VARIABLES) {
                prefixedResult.addElement(LookupElementBuilder.create(variable.name())
                        .withTypeText(variable.type())
                        .withTailText("  " + variable.description(), true)
                        .withBoldness(true));
            }
        }
    }
}
