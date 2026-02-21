// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLTokenTypes;

public class ConcordYAMLFlexLexer extends MergingLexerAdapter {

    private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(YAMLTokenTypes.TEXT);

    private static final int DIRTY_STATE = 239;

    public ConcordYAMLFlexLexer() {
        super(new MyFlexAdapter(new _ConcordYAMLLexer(null)), TOKENS_TO_MERGE);
    }

    private static class MyFlexAdapter extends FlexAdapter {

        MyFlexAdapter(_ConcordYAMLLexer flex) {
            super(flex);
        }

        @Override
        public _ConcordYAMLLexer getFlex() {
            return (_ConcordYAMLLexer)super.getFlex();
        }

        @Override
        public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
            if (initialState != DIRTY_STATE) {
                getFlex().cleanMyState();
            }
            else {
                // That should not occur normally, but some complex lexers (e.g. black and white lexer)
                // require "suspending" of the lexer to pass some template language. In these cases we
                // believe that the same instance of the lexer would be restored (with its internal state)
                initialState = 0;
            }

            super.start(buffer, startOffset, endOffset, initialState);
        }

        @Override
        public int getState() {
            final int state = super.getState();
            if (state == 0 && getFlex().isCleanState()) {
                return 0;
            }
            // Inside the flows section, the lexer carries context (myInsideFlowsSection,
            // myFlowDefIndent, etc.) that cannot be captured in the integer state alone.
            // Returning DIRTY_STATE forces re-lex from a clean point, preserving
            // flow doc highlighting after incremental edits.
            if (state != 0 && !getFlex().isInsideFlowsSection()) {
                return state;
            }
            return DIRTY_STATE;
        }
    }
}
