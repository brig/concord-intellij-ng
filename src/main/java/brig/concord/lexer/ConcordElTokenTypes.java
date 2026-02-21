// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.lexer;

import brig.concord.yaml.YAMLElementType;
import com.intellij.psi.tree.IElementType;

public interface ConcordElTokenTypes {

    /**
     * Lazily-parsed EL expression body (content between ${ and }).
     * Parsed by ElParser when the PSI tree is accessed.
     * Not emitted by lexer directly — created by parser via {@code collapse()}.
     */
    IElementType EL_EXPR = new ConcordElExpressionElementType();

    /** The '${' delimiter that starts an EL expression. */
    IElementType EL_EXPR_START = new YAMLElementType("el expr start");

    /** The '}' delimiter that closes an EL expression. */
    IElementType EL_EXPR_END = new YAMLElementType("el expr end");

    /**
     * Fragment of EL expression body — emitted by lexer, collapsed into {@link #EL_EXPR} by parser.
     * In single-line expressions, there is one EL_EXPR_BODY token.
     * In multi-line expressions, there are multiple EL_EXPR_BODY tokens with SCALAR_EOL/INDENT between them.
     */
    IElementType EL_EXPR_BODY = new YAMLElementType("el expr body");
}
