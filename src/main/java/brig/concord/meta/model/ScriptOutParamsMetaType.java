// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ScriptOutParamsMetaType extends YamlAnyOfType {

    private static final ScriptOutParamsMetaType INSTANCE = new ScriptOutParamsMetaType();

    public static ScriptOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private ScriptOutParamsMetaType() {
        super(List.of(OutVarMetaType.getInstance(), AnyMapMetaType.getInstance()),
                descKey("doc.step.feature.out.description"));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
