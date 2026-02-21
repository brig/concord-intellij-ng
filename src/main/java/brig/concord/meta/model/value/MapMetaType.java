// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import brig.concord.meta.ConcordMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Any key to object
 */
public abstract class MapMetaType extends ConcordMetaType {

    protected MapMetaType() {
    }

    protected MapMetaType(@NotNull TypeProps props) {
        super(props);
    }

    protected abstract YamlMetaType getMapEntryType(String name);

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return new Field(name, getMapEntryType(name));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return Map.of();
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (!(value instanceof YAMLMapping)) {
            problemsHolder.registerProblem(value, ConcordBundle.message("YamlUnknownValuesInspectionBase.error.object.is.required"));
        }
    }
}
