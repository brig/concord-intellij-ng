/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 Concord Plugin Authors
 */
package brig.concord.el.parser;

import brig.concord.el.psi.ElTypes;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;

public class ElParserUtil extends GeneratedParserUtilBase {

    /**
     * External lookahead for namespace function calls: ns:func(args).
     * <p>
     * Checks that the next 4 tokens are: IDENTIFIER COLON IDENTIFIER LPAREN.
     * This distinguishes namespace functions from ternary colon and map entry colon.
     * <p>
     * Matches the LOOKAHEAD(4) from the original ELParser.jjt (line 352).
     */
    public static boolean isFunctionCall(PsiBuilder b, int level) {
        return b.lookAhead(0) == ElTypes.IDENTIFIER
                && b.lookAhead(1) == ElTypes.COLON
                && b.lookAhead(2) == ElTypes.IDENTIFIER
                && b.lookAhead(3) == ElTypes.LPAREN;
    }
}
