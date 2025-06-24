// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package brig.concord.yaml.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLTokenTypes;

public class YAMLFlexLexer extends MergingLexerAdapter {
    private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(YAMLTokenTypes.TEXT);

    private static final int DIRTY_STATE = 239;

    public YAMLFlexLexer() {
        super(new MyFlexAdapter(new _YAMLLexer(null)), TOKENS_TO_MERGE);
    }

    private static class MyFlexAdapter extends FlexAdapter {

        MyFlexAdapter(_YAMLLexer flex) {
            super(flex);
        }

        @Override
        public _YAMLLexer getFlex() {
            return (_YAMLLexer)super.getFlex();
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
            if (state != 0 || getFlex().isCleanState()) {
                return state;
            }
            return DIRTY_STATE;
        }
    }
}
