package brig.concord.yaml.meta.model;

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/**
 * Class for values of any scalar types (string, number, boolean), i.e. no type-specific validation is performed
 */
public class YamlAnyScalarType extends YamlScalarType {
    private static final YamlAnyScalarType SHARED_INSTANCE = new YamlAnyScalarType();

    public static YamlAnyScalarType getInstance() {
        return SHARED_INSTANCE;
    }

    public YamlAnyScalarType() {
        this("any scalar");
    }

    public YamlAnyScalarType(String displayName) {
        super("yaml:any-scalar", displayName);
    }
}
