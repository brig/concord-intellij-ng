package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlEnumType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class LoopMetaType extends ConcordMetaType {

    private static final LoopMetaType INSTANCE = new LoopMetaType();

    private static final Set<String> requiredFeatures = Set.of("items");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "items", AnythingMetaType::getInstance,
            "parallelism", IntegerMetaType::getInstance,
            "mode", ModeType::getInstance
    );

    public static LoopMetaType getInstance() {
        return INSTANCE;
    }

    protected LoopMetaType() {
        super("loop");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFeatures;
    }

    private static class ModeType extends YamlEnumType {

        private static final ModeType INSTANCE = new ModeType();

        public static ModeType getInstance() {
            return INSTANCE;
        }

        protected ModeType() {
            super("Mode");
            setDisplayName("[serial|parallel]");
            withLiterals("serial", "parallel");
        }
    }
}
