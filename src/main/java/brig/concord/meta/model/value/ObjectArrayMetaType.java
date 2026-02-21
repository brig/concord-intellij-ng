// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjectArrayMetaType extends YamlArrayType {

    private static final ObjectArrayMetaType INSTANCE = new ObjectArrayMetaType();

    public static ObjectArrayMetaType getInstance() {
        return INSTANCE;
    }

    public ObjectArrayMetaType() {
        super(AnyMapMetaType.getInstance());
    }

    public ObjectArrayMetaType(@NotNull TypeProps props) {
        super(AnyMapMetaType.getInstance(), props);
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
