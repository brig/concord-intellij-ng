// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
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

import java.util.HashMap;
import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ProfileConfigurationMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ProfileConfigurationMetaType INSTANCE = new ProfileConfigurationMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("runtime", new YamlEnumType("runtime", descKey("doc.configuration.runtime.description"), YamlEnumType.EnumValue.fromLiterals("concord-v2")));
        features.put("debug", new BooleanMetaType(descKey("doc.configuration.debug.description")));
        features.put("entryPoint", new CallMetaType(descKey("doc.configuration.entryPoint.description")));
        features.put("dependencies", new DependenciesMetaType(descKey("doc.configuration.dependencies.description")));
        features.put("extraDependencies", new DependenciesMetaType(descKey("doc.configuration.extraDependencies.description")));
        features.put("arguments", new AnyMapMetaType(descKey("doc.configuration.arguments.description")));
        features.put("meta", new AnyMapMetaType(descKey("doc.configuration.meta.description")));
        features.put("events", EventsMetaType.getInstance());
        features.put("requirements", new AnyMapMetaType(descKey("doc.configuration.requirements.description")));
        features.put("processTimeout", new DurationMetaType(descKey("doc.configuration.processTimeout.description")));
        features.put("suspendTimeout", new DurationMetaType(descKey("doc.configuration.suspendTimeout.description")));
        features.put("exclusive", ProcessExclusiveMetaType.getInstance());
        features.put("out", new StringArrayMetaType(descKey("doc.configuration.out.description")));
        features.put("template", new StringMetaType(descKey("doc.configuration.template.description")));
        features.put("parallelLoopParallelism", new IntegerMetaType(descKey("doc.configuration.parallelLoopParallelism.description")));
    }

    public static ProfileConfigurationMetaType getInstance() {
        return INSTANCE;
    }

    private ProfileConfigurationMetaType() {
        super(descKey("doc.profile.configuration.description"));
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
