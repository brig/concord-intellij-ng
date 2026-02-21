// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.dependency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MavenCoordinateVersionTest {

    @Test
    void isLatestVersionTrue() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:latest");
        assertNotNull(coord);
        assertTrue(coord.isLatestVersion());
    }

    @Test
    void isLatestVersionCaseInsensitive() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:LATEST");
        assertNotNull(coord);
        assertFalse(coord.isLatestVersion());
    }

    @Test
    void isLatestVersionFalseForNormal() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");
        assertNotNull(coord);
        assertFalse(coord.isLatestVersion());
    }

    @Test
    void isResolvableVersionWithDigits() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");
        assertNotNull(coord);
        assertTrue(coord.isResolvableVersion());
    }

    @Test
    void isResolvableVersionSnapshot() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:2.35.0-SNAPSHOT");
        assertNotNull(coord);
        assertTrue(coord.isResolvableVersion());
    }

    @Test
    void isResolvableVersionFalseForPlaceholder() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:PROJECT_VERSION");
        assertNotNull(coord);
        assertFalse(coord.isResolvableVersion());
    }

    @Test
    void isResolvableVersionFalseForLatest() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:latest");
        assertNotNull(coord);
        assertFalse(coord.isResolvableVersion());
    }

    @Test
    void toGA() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");
        assertNotNull(coord);
        assertEquals("com.example:lib", coord.toGA());
    }

    @Test
    void withVersion() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:latest");
        assertNotNull(coord);

        var newCoord = coord.withVersion("2.0.0");
        assertEquals("2.0.0", newCoord.getVersion());
        assertEquals("com.example", newCoord.getGroupId());
        assertEquals("lib", newCoord.getArtifactId());
        assertEquals("mvn://com.example:lib:2.0.0", newCoord.toString());
    }

    @Test
    void withVersionPreservesClassifier() {
        var coord = MavenCoordinate.parse("mvn://com.example:lib:jar:sources:latest");
        assertNotNull(coord);

        var newCoord = coord.withVersion("1.5.0");
        assertEquals("1.5.0", newCoord.getVersion());
        assertEquals("sources", newCoord.getClassifier());
        assertEquals("jar", newCoord.getType());
    }
}
