package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface TaskSchemaProvider {

    @Nullable InputStream getSchemaStream(@NotNull String taskName);
}
