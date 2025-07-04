package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnything;

public class AnythingMetaType extends YamlAnything {

    private static final AnythingMetaType INSTANCE = new AnythingMetaType();

    public static AnythingMetaType getInstance() {
        return INSTANCE;
    }

    private static final Field anyField = new Field("<any-key>", INSTANCE)
            .withAnyName()
            .withRelationSpecificType(Field.Relation.SEQUENCE_ITEM, INSTANCE)
            .withRelationSpecificType(Field.Relation.SCALAR_VALUE, INSTANCE)
            .withEmptyValueAllowed(false);

    @Override
    public Field findFeatureByName(@NotNull String name) {
        return new Field(name, INSTANCE)
                .withAnyName()
                .withRelationSpecificType(Field.Relation.SEQUENCE_ITEM, INSTANCE)
                .withRelationSpecificType(Field.Relation.SCALAR_VALUE, INSTANCE)
                .withEmptyValueAllowed(false);
    }
}
