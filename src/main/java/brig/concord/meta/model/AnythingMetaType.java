package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlAnything;

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
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return anyField;
    }
}
