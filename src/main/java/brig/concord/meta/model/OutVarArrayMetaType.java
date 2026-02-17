// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

/**
 * Array meta-type whose items are {@link OutVarMetaType}, enabling
 * {@code PsiNamedElement} support for scalar out-variable declarations
 * inside array {@code out:} fields (e.g., {@code out: [result1, result2]}).
 */
public class OutVarArrayMetaType extends YamlArrayType {

    private static final OutVarArrayMetaType INSTANCE = new OutVarArrayMetaType();

    public static OutVarArrayMetaType getInstance() {
        return INSTANCE;
    }

    private OutVarArrayMetaType() {
        super(OutVarMetaType.getInstance());
    }
}
