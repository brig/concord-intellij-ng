// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection;

import brig.concord.dependency.MavenCoordinate;
import brig.concord.dependency.TaskRegistry;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class UnresolvedDependencyInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(UnresolvedDependencyInspection.class);
    }

    @Test
    void testUnresolvedDependencyInConfiguration() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                """);

        setFailed("mvn://com.example:lib:1.0.0", "Artifact not found");

        inspection(value("/configuration/dependencies[0]"))
                .expectHighlight("Cannot resolve dependency 'mvn://com.example:lib:1.0.0': Artifact not found");
    }

    @Test
    void testResolvedDependencyNoWarning() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                """);

        // No failed dependencies set — should be clean
        assertNoWarnings();
    }

    @Test
    void testMixedResolvedAndUnresolved() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:good:1.0.0"
                    - "mvn://com.example:bad:2.0.0"
                """);

        setFailed("mvn://com.example:bad:2.0.0", "Connection refused");

        inspection(value("/configuration/dependencies[1]"))
                .expectHighlight("Cannot resolve dependency 'mvn://com.example:bad:2.0.0': Connection refused");
    }

    @Test
    void testExtraDependenciesInProfile() {
        configureFromText("""
                profiles:
                  myProfile:
                    configuration:
                      extraDependencies:
                        - "mvn://com.example:extra:3.0.0"
                """);

        setFailed("mvn://com.example:extra:3.0.0", "Not found");

        inspection(value("/profiles/myProfile/configuration/extraDependencies[0]"))
                .expectHighlight("Cannot resolve dependency 'mvn://com.example:extra:3.0.0': Not found");
    }

    @Test
    void testNonDependencyScalarsNotAffected() {
        configureFromText("""
                configuration:
                  entryPoint: main
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                flows:
                  main:
                    - log: "hello"
                """);

        setFailed("mvn://com.example:lib:1.0.0", "Not found");

        // Only the dependency should have the warning, not other scalars
        inspection(value("/configuration/dependencies[0]"))
                .expectHighlight("Cannot resolve dependency 'mvn://com.example:lib:1.0.0': Not found");
    }

    @Test
    void testNoDependencySections() {
        configureFromText("""
                flows:
                  main:
                    - log: "hello"
                """);

        setFailed("mvn://com.example:lib:1.0.0", "Not found");

        assertNoWarnings();
    }

    private void setFailed(String coordinate, String errorMessage) {
        var coord = Objects.requireNonNull(MavenCoordinate.parse(coordinate), "Invalid coordinate: " + coordinate);
        TaskRegistry.getInstance(getProject())
                .setFailedDependencies(Map.of(coord, errorMessage));
    }
}