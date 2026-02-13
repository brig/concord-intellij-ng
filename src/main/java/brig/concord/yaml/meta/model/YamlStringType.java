package brig.concord.yaml.meta.model;

/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

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
}
