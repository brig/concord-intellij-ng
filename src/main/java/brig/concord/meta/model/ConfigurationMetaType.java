// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.*;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ConfigurationMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ConfigurationMetaType INSTANCE = new ConfigurationMetaType();

    private static final Map<String, YamlMetaType> features = Map.ofEntries(
            Map.entry("runtime", new YamlEnumType("string", descKey("doc.configuration.runtime.description"), YamlEnumType.EnumValue.fromLiterals("concord-v2"))),
            Map.entry("debug", new BooleanMetaType(descKey("doc.configuration.debug.description"))),
            Map.entry("entryPoint", new CallMetaType(descKey("doc.configuration.entryPoint.description"))),
            Map.entry("dependencies", new DependenciesMetaType(descKey("doc.configuration.dependencies.description"))),
            Map.entry("arguments", new AnyMapMetaType(descKey("doc.configuration.arguments.description"))),
            Map.entry("meta", new AnyMapMetaType(descKey("doc.configuration.meta.description"))),
            Map.entry("events", EventsMetaType.getInstance()),
            Map.entry("requirements", new AnyMapMetaType(descKey("doc.configuration.requirements.description"))),
            Map.entry("processTimeout", new DurationMetaType(descKey("doc.configuration.processTimeout.description"))),
            Map.entry("suspendTimeout", new DurationMetaType(descKey("doc.configuration.suspendTimeout.description"))),
            Map.entry("exclusive", ProcessExclusiveMetaType.getInstance()),
            Map.entry("out", new StringArrayMetaType(descKey("doc.configuration.out.description"))),
            Map.entry("template", new StringMetaType(descKey("doc.configuration.template.description"))),
            Map.entry("parallelLoopParallelism", new IntegerMetaType(descKey("doc.configuration.parallelLoopParallelism.description")))
    );

    private ConfigurationMetaType() {
        super(descKey("doc.configuration.description"));
    }

    public static ConfigurationMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }
}
