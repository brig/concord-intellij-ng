// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;

public class IntegerArrayMetaType extends YamlArrayType {

    private static final IntegerArrayMetaType INSTANCE = new IntegerArrayMetaType();

    public static IntegerArrayMetaType getInstance() {
        return INSTANCE;
    }

    public IntegerArrayMetaType() {
        super(IntegerMetaType.getInstance());
    }

    public IntegerArrayMetaType(@NotNull TypeProps props) {
        super(IntegerMetaType.getInstance(), props);
    }
}
