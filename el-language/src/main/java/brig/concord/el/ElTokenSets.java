// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.el;

import brig.concord.el.psi.ElTypes;
import com.intellij.psi.tree.TokenSet;

public final class ElTokenSets {

    public static final TokenSet STRINGS = TokenSet.create(
            ElTypes.SINGLE_QUOTED_STRING,
            ElTypes.DOUBLE_QUOTED_STRING
    );

    public static final TokenSet NUMBERS = TokenSet.create(
            ElTypes.INTEGER_LITERAL,
            ElTypes.FLOAT_LITERAL
    );

    public static final TokenSet KEYWORDS = TokenSet.create(
            ElTypes.TRUE_KEYWORD,
            ElTypes.FALSE_KEYWORD,
            ElTypes.NULL_KEYWORD,
            ElTypes.EMPTY_KEYWORD,
            ElTypes.NOT_KEYWORD,
            ElTypes.AND_KEYWORD,
            ElTypes.OR_KEYWORD,
            ElTypes.DIV_KEYWORD,
            ElTypes.MOD_KEYWORD,
            ElTypes.EQ_KEYWORD,
            ElTypes.NE_KEYWORD,
            ElTypes.LT_KEYWORD,
            ElTypes.GT_KEYWORD,
            ElTypes.LE_KEYWORD,
            ElTypes.GE_KEYWORD,
            ElTypes.INSTANCEOF_KEYWORD
    );

    private ElTokenSets() {
    }
}
