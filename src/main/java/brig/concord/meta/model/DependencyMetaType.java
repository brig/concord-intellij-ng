// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

public class DependencyMetaType extends StringMetaType {

    private static final DependencyMetaType INSTANCE = new DependencyMetaType();

    public static DependencyMetaType getInstance() {
        return INSTANCE;
    }
}
