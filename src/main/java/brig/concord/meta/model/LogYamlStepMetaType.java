// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class LogYamlStepMetaType extends IdentityMetaType {

    private static final LogYamlStepMetaType INSTANCE = new LogYamlStepMetaType();

    public static LogYamlStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(),
            Map.of("logYaml", new StringMetaType(descKey("doc.step.logYaml.key.description").andRequired()))
    );

    private LogYamlStepMetaType() {
        super("logYaml", descKey("doc.step.logYaml.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
