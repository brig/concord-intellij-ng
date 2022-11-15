package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.meta.ConcordMetaType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class MapMetaType extends ConcordMetaType {

    protected MapMetaType(@NotNull String name) {
        super(name);
    }

    protected abstract YamlMetaType getMapEntryType(String name);

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return new Field(name, getMapEntryType(name));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return new HashMap<>();
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        YamlMetaType type = getMapEntryType(null);
        if (type instanceof ConcordMetaType) {
            if (!(value instanceof YAMLMapping)) {
                problemsHolder.registerProblem(value, ConcordBundle.message("YamlUnknownValuesInspectionBase.error.object.is.required"));
            }
        }
        super.validateValue(value, problemsHolder);
    }
}