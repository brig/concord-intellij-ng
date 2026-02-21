// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

/**
 * Marker interface for meta-types that represent {@code out:} parameter containers
 * in step definitions. Used by the delegate factory to provide {@code PsiNamedElement}
 * support for scalar out-variable declarations, enabling Find Usages.
 */
public interface OutVarContainerMetaType {
}