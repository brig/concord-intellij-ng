package brig.concord.dependency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class TaskNameExtractorTest {

    private final TaskNameExtractor extractor = new TaskNameExtractor();

    @Test
    void extractsTaskNamesFromRealJar() {
        var jarPath = getTestJarPath();
        var names = extractor.extract(jarPath);

        assertEquals(2, names.size());
        assertTrue(names.contains("git"));
        assertTrue(names.contains("github"));
    }

    @Test
    void returnsEmptyForJarWithoutSisuIndex(@TempDir Path tempDir) throws IOException {
        var jarPath = createEmptyJar(tempDir);
        var names = extractor.extract(jarPath);

        assertTrue(names.isEmpty());
    }

    @Test
    void returnsEmptyForNonExistentJar() {
        var names = extractor.extract(Path.of("/nonexistent.jar"));

        assertTrue(names.isEmpty());
    }

    @Test
    void skipsClassesMissingFromJar(@TempDir Path tempDir) throws IOException {
        var jarPath = createJarWithSisuIndex(tempDir, "com.example.Missing\n");
        var names = extractor.extract(jarPath);

        assertTrue(names.isEmpty());
    }

    private Path getTestJarPath() {
        var url = getClass().getResource("/dependency/git-task-2.12.0.jar");
        assertNotNull(url, "Test JAR not found on classpath");
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Path createEmptyJar(Path dir) throws IOException {
        var path = dir.resolve("empty.jar");
        try (var jos = new JarOutputStream(Files.newOutputStream(path))) {
            // empty JAR
        }
        return path;
    }

    private Path createJarWithSisuIndex(Path dir, String content) throws IOException {
        var path = dir.resolve("test.jar");
        try (var jos = new JarOutputStream(Files.newOutputStream(path))) {
            jos.putNextEntry(new JarEntry("META-INF/sisu/javax.inject.Named"));
            jos.write(content.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return path;
    }
}
