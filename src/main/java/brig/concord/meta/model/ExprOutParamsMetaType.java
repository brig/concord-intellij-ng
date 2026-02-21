// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ExprOutParamsMetaType extends YamlAnyOfType {

    private static final ExprOutParamsMetaType INSTANCE = new ExprOutParamsMetaType();

    public static ExprOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private ExprOutParamsMetaType() {
        super(List.of(OutVarMetaType.getInstance(), AnyMapMetaType.getInstance()),
                descKey("doc.step.feature.out.description"));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
