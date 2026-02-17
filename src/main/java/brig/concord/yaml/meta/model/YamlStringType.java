// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import org.jetbrains.annotations.NotNull;

public class YamlStringType extends YamlScalarType {

    private static final YamlStringType SHARED_INSTANCE = new YamlStringType();

    public static YamlStringType getInstance() {
        return SHARED_INSTANCE;
    }

    public YamlStringType() {
        this("string");
    }

    public YamlStringType(String typeName) {
        super(typeName);
    }

    public YamlStringType(String typeName, @NotNull TypeProps props) {
        super(typeName, props);
    }
}
