// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.resolve.YAMLAliasReference;

/** See <a href="http://www.yaml.org/spec/1.2/spec.html#id2786196">7.1. Alias Nodes</a> */
public interface YAMLAlias extends YAMLValue {
    @NotNull
    String getAliasName();

    @Override
    YAMLAliasReference getReference();
}
