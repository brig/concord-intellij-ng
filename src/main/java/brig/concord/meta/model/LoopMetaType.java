package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LoopMetaType extends ConcordMetaType {

    private static final LoopMetaType INSTANCE = new LoopMetaType();

    private static final Set<String> requiredFeatures = Set.of("items");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "items", LoopItemsMetaType::getInstance,
            "parallelism", () -> AnyOfType.anyOf(IntegerMetaType.getInstance(), ExpressionMetaType.getInstance()),
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
            super("Mode", "[serial|parallel]");
            withLiterals("serial", "parallel");
        }
    }
}
