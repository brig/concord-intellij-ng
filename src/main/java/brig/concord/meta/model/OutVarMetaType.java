// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

/**
 * String meta-type for scalar out-variable declarations (e.g., {@code out: result}).
 * Implements {@link OutVarContainerMetaType} so the delegate factory can identify
 * these scalars and provide {@code PsiNamedElement} support for Find Usages.
 */
public class OutVarMetaType extends StringMetaType implements OutVarContainerMetaType {

    private static final OutVarMetaType INSTANCE = new OutVarMetaType();

    public static OutVarMetaType getInstance() {
        return INSTANCE;
    }

    private OutVarMetaType() {
    }
}
