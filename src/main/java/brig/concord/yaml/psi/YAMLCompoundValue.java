// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import org.jetbrains.annotations.NotNull;

public interface YAMLCompoundValue extends YAMLValue {
    @NotNull
    String getTextValue();
}
