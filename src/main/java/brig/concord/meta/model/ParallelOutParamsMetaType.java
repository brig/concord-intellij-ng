// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ParallelOutParamsMetaType extends YamlAnyOfType {

    private static final ParallelOutParamsMetaType INSTANCE = new ParallelOutParamsMetaType();

    public static ParallelOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private ParallelOutParamsMetaType() {
        super(List.of(OutVarMetaType.getInstance(), AnyMapMetaType.getInstance(), OutVarArrayMetaType.getInstance()),
                descKey("doc.step.feature.out.description"));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
