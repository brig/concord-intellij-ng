// SPDX-License-Identifier: Apache-2.0
package brig.concord.completion;

import brig.concord.ConcordTypes;
import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.lexer.FlowDocTokenTypes;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.YAMLTokenTypes;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FlowDocCompletionContributor extends CompletionContributor {

    private static final List<String> TYPE_NAMES = createTypeNames();

    private static List<String> createTypeNames() {
        var names = new ArrayList<String>();
        for (var type : ConcordTypes.aliases().keySet()) {
            names.add(type);
            names.add(type + "[]");
        }
        return List.copyOf(names);
    }

    private static final List<String> KEYWORDS = List.of("mandatory", "required", "optional");

    public FlowDocCompletionContributor() {
        // Type completion - after colon in flow doc parameter
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new TypeCompletionProvider());

        // Keyword completion - after type in flow doc parameter
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new KeywordCompletionProvider());
    }

    private static boolean isInFlowDoc(@NotNull PsiElement element) {
        if (!(element.getContainingFile() instanceof ConcordFile)) {
            return false;
        }

        var parent = element;
        while (parent != null) {
            var elementType = parent.getNode().getElementType();
            if (elementType == FlowDocElementTypes.FLOW_DOCUMENTATION) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private static IElementType getPrevTokenType(@NotNull PsiElement element) {
        var prev = element.getPrevSibling();
        while (prev != null) {
            var type = prev.getNode().getElementType();
            if (type != YAMLTokenTypes.WHITESPACE) {
                return type;
            }
            prev = prev.getPrevSibling();
        }
        return null;
    }

    private static class TypeCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();

            if (!isInFlowDoc(position)) {
                return;
            }

            // Check if we're after a colon (type position)
            var prevType = getPrevTokenType(position);
            if (prevType != FlowDocTokenTypes.FLOW_DOC_COLON) {
                // Also check if we're at a type token that's being edited
                var currentType = position.getNode().getElementType();
                if (currentType != FlowDocTokenTypes.FLOW_DOC_TYPE &&
                        currentType != FlowDocTokenTypes.FLOW_DOC_ARRAY_TYPE) {
                    return;
                }
            }

            for (var name : TYPE_NAMES) {
                var isArray = name.endsWith("[]");
                result.addElement(LookupElementBuilder.create(name)
                        .withTypeText(isArray ? "array type" : "type")
                        .withBoldness(!isArray));
            }
        }
    }

    private static class KeywordCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();

            if (!isInFlowDoc(position)) {
                return;
            }

            // Check if we're after a comma following type (keyword position)
            var prevType = getPrevTokenType(position);
            if (prevType != FlowDocTokenTypes.FLOW_DOC_COMMA) {
                // Also check if we're at a keyword token that's being edited
                var currentType = position.getNode().getElementType();
                if (currentType != FlowDocTokenTypes.FLOW_DOC_MANDATORY &&
                        currentType != FlowDocTokenTypes.FLOW_DOC_OPTIONAL &&
                        currentType != FlowDocTokenTypes.FLOW_DOC_UNKNOWN_KEYWORD) {
                    return;
                }
            }

            for (var keyword : KEYWORDS) {
                result.addElement(LookupElementBuilder.create(keyword)
                        .withTypeText("keyword")
                        .withBoldness("mandatory".equals(keyword) || "required".equals(keyword)));
            }
        }
    }
}
