// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;

public class RegexpArrayMetaType extends YamlArrayType {

    private static final RegexpArrayMetaType INSTANCE = new RegexpArrayMetaType();

    public static RegexpArrayMetaType getInstance() {
        return INSTANCE;
    }

    public RegexpArrayMetaType() {
        super(RegexpMetaType.getInstance());
    }

    public RegexpArrayMetaType(@NotNull TypeProps props) {
        super(RegexpMetaType.getInstance(), props);
    }
}
