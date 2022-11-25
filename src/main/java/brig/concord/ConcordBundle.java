package brig.concord;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class ConcordBundle extends DynamicBundle {
    @NonNls
    public static final String BUNDLE = "messages.ConcordBundle";
    private static final ConcordBundle INSTANCE = new ConcordBundle();

    private ConcordBundle() {
        super(BUNDLE);
    }

    @NotNull
    public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}