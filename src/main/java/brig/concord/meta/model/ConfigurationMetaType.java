package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.documentation.Documented;
import brig.concord.meta.ConcordMetaType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.*;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ConfigurationMetaType extends ConcordMetaType implements Documented {

    private static final ConfigurationMetaType INSTANCE = new ConfigurationMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("runtime", () -> new YamlEnumType("runtime").withLiterals("concord-v2"));
        features.put("debug", YamlBooleanType::getSharedInstance);
        features.put("entryPoint", StringMetaType::getInstance);
        features.put("dependencies", DependenciesMetaType::getInstance);
        features.put("arguments", AnyMapMetaType::getInstance);
        features.put("meta", AnyMapMetaType::getInstance);
        features.put("events", EventsMetaType::getInstance);
        features.put("requirements", AnyMapMetaType::getInstance);
        features.put("processTimeout", DurationMetaType::getInstance);
        features.put("suspendTimeout", DurationMetaType::getInstance);
        features.put("exclusive", ProcessExclusiveMetaType::getInstance);
        features.put("out", StringArrayMetaType::getInstance);
        features.put("template", StringMetaType::getInstance);
        features.put("parallelLoopParallelism", () -> YamlIntegerType.getInstance(false));
    }

    public static ConfigurationMetaType getInstance() {
        return INSTANCE;
    }

    private ConfigurationMetaType() {
        super("Configuration");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    public String getDescription() {
        return ConcordBundle.message("Configuration.description");
    }
}
