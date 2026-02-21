// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlIntegerType;
import org.jetbrains.annotations.NotNull;

public class IntegerMetaType extends YamlIntegerType {

    private static final IntegerMetaType INSTANCE = new IntegerMetaType();

    public static IntegerMetaType getInstance() {
        return INSTANCE;
    }

    public IntegerMetaType() {
        super(false);
    }

    public IntegerMetaType(@NotNull TypeProps props) {
        super(false, props);
    }
}
