// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

/**
 * Class for values of any scalar types (string, number, boolean), i.e. no type-specific validation is performed
 */
public class YamlAnyScalarType extends YamlScalarType {
    private static final YamlAnyScalarType SHARED_INSTANCE = new YamlAnyScalarType();

    public static YamlAnyScalarType getInstance() {
        return SHARED_INSTANCE;
    }

    public YamlAnyScalarType() {
        super("any scalar");
    }
}
