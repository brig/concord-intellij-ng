// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class InParamsMetaType extends YamlAnyOfType {

    private static final InParamsMetaType INSTANCE = new InParamsMetaType();

    public static InParamsMetaType getInstance() {
        return INSTANCE;
    }

    private InParamsMetaType() {
        super(List.of(ExpressionMetaType.getInstance(), AnyMapMetaType.getInstance()),
                descKey("doc.step.feature.in.description"));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
