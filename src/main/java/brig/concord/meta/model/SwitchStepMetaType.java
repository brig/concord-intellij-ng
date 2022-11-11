package brig.concord.meta.model;

import brig.concord.meta.ConcordMapMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SwitchStepMetaType extends StepMetaType {

    private static final SwitchStepMetaType INSTANCE = new SwitchStepMetaType();

    public static SwitchStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "switch", SwitchEntryType::getInstance,
            "default", StepsMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance);

    protected SwitchStepMetaType() {
        super("Switch", "switch", Set.of("switch"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    private static class SwitchEntryType extends ConcordMapMetaType {

        private static final SwitchEntryType INSTANCE = new SwitchEntryType();

        public static SwitchEntryType getInstance() {
            return INSTANCE;
        }

        protected SwitchEntryType() {
            super("Switch entry");
        }

        @Override
        protected YamlMetaType getMapEntryType(String name) {
            return StepsMetaType.getInstance();
        }
    }
}
