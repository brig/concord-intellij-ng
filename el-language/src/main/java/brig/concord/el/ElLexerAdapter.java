// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.el;

import com.intellij.lexer.FlexAdapter;

public class ElLexerAdapter extends FlexAdapter {

    public ElLexerAdapter() {
        super(new ElLexer(null));
    }
}
