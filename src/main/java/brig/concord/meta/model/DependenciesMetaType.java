// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;

public class DependenciesMetaType extends YamlArrayType {

    private static final DependenciesMetaType INSTANCE = new DependenciesMetaType();

    public static DependenciesMetaType getInstance() {
        return INSTANCE;
    }

    DependenciesMetaType() {
        super(DependencyMetaType.getInstance());
    }

    DependenciesMetaType(@NotNull TypeProps props) {
        super(DependencyMetaType.getInstance(), props);
    }
}
