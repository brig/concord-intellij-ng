// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;

public class StringArrayMetaType extends YamlArrayType {

    private static final StringArrayMetaType INSTANCE = new StringArrayMetaType();

    public static StringArrayMetaType getInstance() {
        return INSTANCE;
    }

    public StringArrayMetaType() {
        super(StringMetaType.getInstance());
    }

    public StringArrayMetaType(@NotNull TypeProps props) {
        super(StringMetaType.getInstance(), props);
    }
}
