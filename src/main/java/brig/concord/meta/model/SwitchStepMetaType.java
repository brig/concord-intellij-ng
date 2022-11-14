package brig.concord.meta.model;

import brig.concord.meta.ConcordMapMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SwitchStepMetaType extends StepMetaType {

    private static final SwitchStepMetaType INSTANCE = new SwitchStepMetaType();

    public static SwitchStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "switch", ExpressionMetaType::getInstance,
            "default", StepsMetaType::getInstance);

    protected SwitchStepMetaType() {
        super("Switch", "switch", Set.of("switch"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return Collections.emptyMap();
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
//        Field f = super.findFeatureByName(name);
//        if (f != null) {
//            return f;
//        }
//
        if (name.equals("switch")) {
            return new Field(name, ExpressionMetaType::getInstance);
        }

        return new Field(name, StepsMetaType.getInstance());
    }
}
