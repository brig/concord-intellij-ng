// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model.call;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class CallOutParamsMetaType extends YamlAnyOfType {

    private static final CallOutParamsMetaType INSTANCE = new CallOutParamsMetaType();

    public static CallOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private CallOutParamsMetaType() {
        super(List.of(CallOutValueMetaType.getInstance(), AnyMapMetaType.getInstance(), new YamlArrayType(CallOutValueMetaType.getInstance())),
                descKey("doc.step.feature.out.description"));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }

}
