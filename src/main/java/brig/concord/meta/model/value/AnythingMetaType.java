// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlAnything;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

public class AnythingMetaType extends YamlAnything {

    public static boolean isInstance(YamlMetaType type) {
        return type instanceof AnythingMetaType;
    }

    public AnythingMetaType() {
    }

    public AnythingMetaType(@NotNull TypeProps props) {
        super(props);
    }

    private static final AnythingMetaType INSTANCE = new AnythingMetaType();

    public static AnythingMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    public Field findFeatureByName(@NotNull String name) {
        return new Field(name, INSTANCE)
                .withAnyName()
                .withRelationSpecificType(Field.Relation.SEQUENCE_ITEM, INSTANCE)
                .withRelationSpecificType(Field.Relation.SCALAR_VALUE, INSTANCE)
                .withEmptyValueAllowed(false);
    }
}
