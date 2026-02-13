package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import brig.concord.meta.ConcordMetaType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;

import java.util.Map;

/**
 * Any key to object
 */
public abstract class MapMetaType extends ConcordMetaType {

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
