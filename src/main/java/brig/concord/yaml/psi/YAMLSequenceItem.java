// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface YAMLSequenceItem extends YAMLPsiElement {
    @Nullable
    YAMLValue getValue();
    @NotNull
    Collection<YAMLKeyValue> getKeysValues();

    int getItemIndex();
}
