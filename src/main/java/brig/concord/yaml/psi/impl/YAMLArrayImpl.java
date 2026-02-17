// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLSequence;

public class YAMLArrayImpl extends YAMLSequenceImpl implements YAMLSequence {
    public YAMLArrayImpl(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML array";
    }
}
