// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.schema.SchemaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class TaskInfoExtractorTest {

    private final TaskInfoExtractor extractor = new TaskInfoExtractor();


    @Test
    void extractReturnsTaskInfos() {
        var jarPath = getTestJarPath();
        var taskInfos = extractor.extract(jarPath);

        assertEquals(2, taskInfos.size());
        assertTrue(taskInfos.containsKey("git"));
        assertTrue(taskInfos.containsKey("github"));

        var gitInfo = taskInfos.get("git");
        assertNotNull(gitInfo);
        var methodNames = gitInfo.methods().stream()
                .map(TaskMethod::name)
                .toList();
        assertFalse(methodNames.contains("execute"), "execute should be excluded");
        assertFalse(methodNames.contains("equals"), "equals should be excluded");
        assertFalse(methodNames.contains("hashCode"), "hashCode should be excluded");
        assertFalse(methodNames.contains("toString"), "toString should be excluded");
    }

    @Test
    void extractReturnsEmptyForJarWithoutSisuIndex(@TempDir Path tempDir) throws IOException {
        var jarPath = createEmptyJar(tempDir);
        var taskInfos = extractor.extract(jarPath);

        assertTrue(taskInfos.isEmpty());
    }

    @Test
    void toSchemaTypeMapsJavaTypes() {
        assertEquals(SchemaType.Scalar.STRING, TaskInfoExtractor.toSchemaType(Type.getType(String.class)));
        assertEquals(SchemaType.Scalar.BOOLEAN, TaskInfoExtractor.toSchemaType(Type.BOOLEAN_TYPE));
        assertEquals(SchemaType.Scalar.BOOLEAN, TaskInfoExtractor.toSchemaType(Type.getType(Boolean.class)));
        assertEquals(SchemaType.Scalar.INTEGER, TaskInfoExtractor.toSchemaType(Type.INT_TYPE));
        assertEquals(SchemaType.Scalar.INTEGER, TaskInfoExtractor.toSchemaType(Type.LONG_TYPE));
        assertEquals(SchemaType.Scalar.INTEGER, TaskInfoExtractor.toSchemaType(Type.getType(Integer.class)));
        assertEquals(SchemaType.Scalar.OBJECT, TaskInfoExtractor.toSchemaType(Type.getType(java.util.Map.class)));
        assertEquals(SchemaType.Array.ANY, TaskInfoExtractor.toSchemaType(Type.getType(java.util.List.class)));
        assertEquals(SchemaType.Array.ANY, TaskInfoExtractor.toSchemaType(Type.getType(String[].class)));
        assertEquals(new SchemaType.Any("void"), TaskInfoExtractor.toSchemaType(Type.VOID_TYPE));
    }

    @Test
    void toSchemaTypePreservesUnknownTypeLabel() {
        var dateType = TaskInfoExtractor.toSchemaType(Type.getType(java.util.Date.class));
        assertInstanceOf(SchemaType.Any.class, dateType);
        assertEquals("Date", ((SchemaType.Any) dateType).label());
        assertEquals("Date", SchemaType.displayName(dateType));
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
}
