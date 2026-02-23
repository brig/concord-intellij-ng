// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class MvnJsonParser {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private MvnJsonParser() {
    }

    /**
     * Reads mvn.json from the given path.
     * Returns an empty config if the file does not exist.
     *
     * @throws IOException         if an I/O error occurs
     * @throws JsonSyntaxException if the file contains malformed JSON
     */
    public static @NotNull MvnJsonConfig read(@NotNull Path path) throws IOException {
        if (!Files.exists(path)) {
            return new MvnJsonConfig();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            var config = GSON.fromJson(reader, MvnJsonConfig.class);
            return config != null ? config : new MvnJsonConfig();
        }
    }

    /**
     * Writes mvn.json to the given path using atomic write (temp file + move).
     * Creates parent directories if needed.
     *
     * @throws IOException if an I/O error occurs
     */
    public static void write(@NotNull Path path, @NotNull MvnJsonConfig config) throws IOException {
        var parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        var tmpDir = parent != null ? parent : path.toAbsolutePath().getParent();
        var tmp = Files.createTempFile(tmpDir, "mvn", ".json.tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(tmp)) {
                GSON.toJson(config, writer);
            }
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
    }
}
