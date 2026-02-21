// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyCollectorTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void collectFromConfiguration() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.walmartlabs.concord.plugins.basic:http-tasks:2.35.0"
                    - "mvn://com.walmartlabs.concord.plugins.basic:slack-tasks:2.35.0"
                flows:
                  main:
                    - log: "hello"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var deps = ReadAction.compute(collector::collectAll);

        assertEquals(2, deps.size());
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("http-tasks")));
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("slack-tasks")));
    }

    @Test
    void collectFromExtraDependencies() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:dep1:1.0.0"
                  extraDependencies:
                    - "mvn://com.example:dep2:1.0.0"
                flows:
                  main:
                    - log: "hello"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var deps = ReadAction.compute(() -> collector.collectAll());

        assertEquals(2, deps.size());
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("dep1")));
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("dep2")));
    }

    @Test
    void collectFromProfiles() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:base:1.0.0"
                profiles:
                  dev:
                    configuration:
                      dependencies:
                        - "mvn://com.example:dev-only:1.0.0"
                  prod:
                    configuration:
                      dependencies:
                        - "mvn://com.example:prod-only:1.0.0"
                      extraDependencies:
                        - "mvn://com.example:prod-extra:1.0.0"
                flows:
                  main:
                    - log: "hello"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var deps = ReadAction.compute(collector::collectAll);

        assertEquals(4, deps.size());
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("base")));
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("dev-only")));
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("prod-only")));
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("prod-extra")));
    }

    @Test
    void deduplicateSameDependencies() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:shared:1.0.0"
                profiles:
                  dev:
                    configuration:
                      dependencies:
                        - "mvn://com.example:shared:1.0.0"
                flows:
                  main:
                    - log: "hello"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var deps = ReadAction.compute(() -> collector.collectAll());

        assertEquals(1, deps.size());
        assertEquals("shared", deps.iterator().next().getArtifactId());
    }

    @Test
    void ignoreInvalidDependencies() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:valid:1.0.0"
                    - "invalid-format"
                    - "http://not-maven.com/artifact.jar"
                flows:
                  main:
                    - log: "hello"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var deps = ReadAction.compute(collector::collectAll);

        assertEquals(1, deps.size());
        assertEquals("valid", deps.iterator().next().getArtifactId());
    }

    @Test
    void collectByScope() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:root-dep:1.0.0"
                flows:
                  main:
                    - log: "hello"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var scopeDeps = ReadAction.compute(collector::collectByScope);

        assertEquals(1, scopeDeps.size());
        var first = scopeDeps.getFirst();
        assertFalse(first.isEmpty());
        assertEquals(1, first.occurrences().size());
        assertEquals("root-dep", first.occurrences().get(0).coordinate().getArtifactId());
    }

    @Test
    void emptyDependencies() {
        createFile("concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "hello"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var deps = ReadAction.compute(collector::collectAll);

        assertTrue(deps.isEmpty());
    }

    @Test
    void collectFromMultipleFilesInScope() {
        // Root file with resources pattern
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:root-dep:1.0.0"
                resources:
                  concord:
                    - "glob:concord/*.concord.yaml"
                flows:
                  main:
                    - log: "hello"
                """);

        // Additional file in scope
        createFile("concord/extra.concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:extra-dep:1.0.0"
                """);

        var collector = DependencyCollector.getInstance(getProject());
        var deps = ReadAction.compute(collector::collectAll);

        assertEquals(2, deps.size());
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("root-dep")));
        assertTrue(deps.stream().anyMatch(d -> d.getArtifactId().equals("extra-dep")));
    }
}
