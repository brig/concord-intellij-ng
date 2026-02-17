// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

public class DependencyMetaType extends StringMetaType {

    private static final DependencyMetaType INSTANCE = new DependencyMetaType();

    public static DependencyMetaType getInstance() {
        return INSTANCE;
    }
}
