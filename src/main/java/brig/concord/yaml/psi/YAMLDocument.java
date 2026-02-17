// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import org.jetbrains.annotations.Nullable;

public interface YAMLDocument extends YAMLPsiElement {
    @Nullable
    YAMLValue getTopLevelValue();
}
