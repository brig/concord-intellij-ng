package brig.concord.yaml.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import static brig.concord.ConcordBundle.BUNDLE;

public record TypeProps(
        @Nullable @PropertyKey(resourceBundle = BUNDLE) String descriptionKey,
        boolean isRequired
) {

    public static TypeProps desc(@NotNull @PropertyKey(resourceBundle = BUNDLE) String descriptionKey) {
        return new TypeProps(descriptionKey, false);
    }

    public static TypeProps required() {
        return new TypeProps(null, true);
    }

    public TypeProps andRequired() {
        return new TypeProps(descriptionKey, true);
    }
}
