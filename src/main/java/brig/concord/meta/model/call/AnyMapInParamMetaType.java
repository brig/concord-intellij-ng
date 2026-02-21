// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model.call;

import brig.concord.meta.model.value.AnyMapMetaType;

public class AnyMapInParamMetaType extends AnyMapMetaType implements CallInParamMetaType {

    private static final AnyMapInParamMetaType INSTANCE = new AnyMapInParamMetaType();

    public static AnyMapInParamMetaType getInstance() {
        return INSTANCE;
    }
}
