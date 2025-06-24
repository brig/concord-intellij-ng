package brig.concord.yaml.psi;

// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import org.jetbrains.annotations.NotNull;

public interface YAMLCompoundValue extends YAMLValue {
    @NotNull
    String getTextValue();
}
