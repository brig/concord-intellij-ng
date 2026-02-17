// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A collection representing a sequence of items
 */
public interface YAMLSequence extends YAMLCompoundValue {
    @NotNull
    List<YAMLSequenceItem> getItems();

    void addItem(@NotNull YAMLSequenceItem item);
}
