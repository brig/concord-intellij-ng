package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

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
