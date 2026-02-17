// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** See <a href="http://www.yaml.org/spec/1.2/spec.html#id2785586">6.9.2. Node Anchors</a> */
public interface YAMLAnchor extends YAMLPsiElement, PsiNameIdentifierOwner {
    @NotNull
    @Override
    String getName();

    /** @return sub-tree YAML value marked by this anchor */
    @Nullable
    YAMLValue getMarkedValue();
}
