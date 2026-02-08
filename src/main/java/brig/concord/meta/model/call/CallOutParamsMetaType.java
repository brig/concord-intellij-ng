package brig.concord.meta.model.call;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.psi.YAMLValue;

import java.util.List;

public class CallOutParamsMetaType extends YamlAnyOfType {

    private static final CallOutParamsMetaType INSTANCE = new CallOutParamsMetaType();

    public static CallOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected CallOutParamsMetaType() {
        super("out params [object|array|string]", List.of(StringMetaType.getInstance(), AnyMapMetaType.getInstance(), StringArrayMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);
    }
}
