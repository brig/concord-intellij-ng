package brig.concord.yaml.psi;

// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.resolve.YAMLAliasReference;

/** See <a href="http://www.yaml.org/spec/1.2/spec.html#id2786196">7.1. Alias Nodes</a> */
public interface YAMLAlias extends YAMLValue {
    @NotNull
    String getAliasName();

    @Override
    YAMLAliasReference getReference();
}
