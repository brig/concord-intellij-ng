// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.dependency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class MavenCoordinateTest {

    @Test
    void parseSimple() {
        var coord = MavenCoordinate.parse("mvn://com.walmartlabs.concord.plugins.basic:http-tasks:2.35.0");

        assertNotNull(coord);
        assertEquals("com.walmartlabs.concord.plugins.basic", coord.getGroupId());
        assertEquals("http-tasks", coord.getArtifactId());
        assertEquals("2.35.0", coord.getVersion());
        assertNull(coord.getClassifier());
        assertEquals("jar", coord.getType());
    }

    @Test
    void parseWithClassifierAndType() {
        var coord = MavenCoordinate.parse("mvn://com.example:artifact:jar:sources:1.0.0");

        assertNotNull(coord);
        assertEquals("com.example", coord.getGroupId());
        assertEquals("artifact", coord.getArtifactId());
        assertEquals("1.0.0", coord.getVersion());
        assertEquals("sources", coord.getClassifier());
        assertEquals("jar", coord.getType());
    }

    @Test
    void parseInvalidFormat() {
        assertNull(MavenCoordinate.parse(null));
        assertNull(MavenCoordinate.parse(""));
        assertNull(MavenCoordinate.parse("   "));
        assertNull(MavenCoordinate.parse("invalid"));
        assertNull(MavenCoordinate.parse("mvn://only:two"));
        assertNull(MavenCoordinate.parse("http://com.example:artifact:1.0.0"));
    }

    @Test
    void repositoryPath() {
        var coord = MavenCoordinate.parse("mvn://com.walmartlabs.concord.plugins.basic:http-tasks:2.35.0");

        assertNotNull(coord);
        var expected = Path.of(
                "com/walmartlabs/concord/plugins/basic",
                "http-tasks",
                "2.35.0",
                "http-tasks-2.35.0.jar"
        );
        assertEquals(expected, coord.getRepositoryPath());
    }

    @Test
    void repositoryPathWithClassifier() {
        var coord = MavenCoordinate.parse("mvn://com.example:artifact:jar:sources:1.0.0");

        assertNotNull(coord);
        var expected = Path.of(
                "com/example",
                "artifact",
                "1.0.0",
                "artifact-1.0.0-sources.jar"
        );
        assertEquals(expected, coord.getRepositoryPath());
    }

    @Test
    void parseWithExtension() {
        var coord = MavenCoordinate.parse("mvn://com.example:artifact:war:1.0.0");

        assertNotNull(coord);
        assertEquals("com.example", coord.getGroupId());
        assertEquals("artifact", coord.getArtifactId());
        assertEquals("1.0.0", coord.getVersion());
        assertNull(coord.getClassifier());
        assertEquals("war", coord.getType());
    }

    @Test
    void repositoryPathWithExtension() {
        var coord = MavenCoordinate.parse("mvn://com.example:artifact:war:1.0.0");

        assertNotNull(coord);
        var expected = Path.of("com/example", "artifact", "1.0.0", "artifact-1.0.0.war");
        assertEquals(expected, coord.getRepositoryPath());
    }

    @Test
    void toGav() {
        var coord = MavenCoordinate.parse("mvn://com.example:artifact:1.0.0");

        assertNotNull(coord);
        assertEquals("com.example:artifact:1.0.0", coord.toGav());
    }

    @Test
    void equality() {
        var coord1 = MavenCoordinate.parse("mvn://com.example:artifact:1.0.0");
        Assertions.assertNotNull(coord1);
        var coord2 = MavenCoordinate.parse("mvn://com.example:artifact:1.0.0");
        Assertions.assertNotNull(coord2);
        var coord3 = MavenCoordinate.parse("mvn://com.example:artifact:2.0.0");
        Assertions.assertNotNull(coord3);

        assertEquals(coord1, coord2);
        assertEquals(coord1.hashCode(), coord2.hashCode());
        assertNotEquals(coord1, coord3);
    }

    @Test
    void parseRejectsPathTraversal() {
        assertNull(MavenCoordinate.parse("mvn://com.example/../etc:artifact:1.0.0"));
        assertNull(MavenCoordinate.parse("mvn://com.example:../artifact:1.0.0"));
        assertNull(MavenCoordinate.parse("mvn://com.example:artifact:../1.0.0"));
        assertNull(MavenCoordinate.parse("mvn://com.example:/artifact:1.0.0"));
        assertNull(MavenCoordinate.parse("mvn://com.example:artifact:jar:../../etc/passwd:1.0.0"),
                "classifier with path traversal should be rejected");
        assertNull(MavenCoordinate.parse("mvn://com.example:artifact:../jar:1.0.0"),
                "type with path traversal should be rejected");
    }

    @Test
    void parseGroovy() {
        var coord = MavenCoordinate.parse("mvn://org.codehaus.groovy:groovy-all:pom:2.5.21");

        assertNotNull(coord);
        assertEquals("org.codehaus.groovy", coord.getGroupId());
        assertEquals("groovy-all", coord.getArtifactId());
        assertEquals("2.5.21", coord.getVersion());
        assertNull(coord.getClassifier());
        assertEquals("pom", coord.getType());
    }
}
