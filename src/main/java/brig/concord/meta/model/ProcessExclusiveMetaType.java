package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlEnumType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ProcessExclusiveMetaType extends ConcordMetaType {

    private static final ProcessExclusiveMetaType INSTANCE = new ProcessExclusiveMetaType();

    private static final Set<String> requiredFeatures = Set.of("group");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "group", StringMetaType::getInstance,
            "mode", ModeType::getInstance
    );

    public static ProcessExclusiveMetaType getInstance() {
        return INSTANCE;
    }

    protected ProcessExclusiveMetaType() {
        super("Exclusive");
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
            setDisplayName("[cancel|cancelOld|wait]");
            withLiterals("cancel", "cancelOld", "wait");
        }
    }
}
