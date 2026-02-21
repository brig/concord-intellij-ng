// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlAnything;

public class LoopArrayItemMetaType extends YamlAnything {

    private static final LoopArrayItemMetaType INSTANCE = new LoopArrayItemMetaType();

    public static LoopArrayItemMetaType getInstance() {
        return INSTANCE;
    }
}
