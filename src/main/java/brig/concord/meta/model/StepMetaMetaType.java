// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class StepMetaMetaType extends AnyMapMetaType {

    private static final StepMetaMetaType INSTANCE = new StepMetaMetaType();

    public static StepMetaMetaType getInstance() {
        return INSTANCE;
    }

    private StepMetaMetaType() {
        super(descKey("doc.step.feature.meta.description"));
    }
}
