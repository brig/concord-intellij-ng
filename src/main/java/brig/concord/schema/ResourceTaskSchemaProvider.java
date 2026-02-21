// SPDX-License-Identifier: Apache-2.0
package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class ResourceTaskSchemaProvider implements TaskSchemaProvider {

    @Override
    public @Nullable InputStream getSchemaStream(@NotNull String taskName) {
        var path = "/taskSchema/" + taskName + ".schema.json";
        return ResourceTaskSchemaProvider.class.getResourceAsStream(path);
    }
}
