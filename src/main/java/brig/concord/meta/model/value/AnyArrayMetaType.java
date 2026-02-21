// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;

public class AnyArrayMetaType extends YamlArrayType {

    private static final AnyArrayMetaType INSTANCE = new AnyArrayMetaType();

    public static AnyArrayMetaType getInstance() {
        return INSTANCE;
    }

    public AnyArrayMetaType() {
        super(AnythingMetaType.getInstance());
    }

    public AnyArrayMetaType(@NotNull TypeProps props) {
        super(AnythingMetaType.getInstance(), props);
    }
}
