// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection;

import brig.concord.dependency.MavenCoordinate;
import brig.concord.dependency.TaskRegistry;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.Test;

import java.util.*;

class ServerOnlyDependencyInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(ServerOnlyDependencyInspection.class);
    }

    @Test
    void testLatestVersionShowsInfo() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:latest"
                """);

        setSkipped("mvn://com.example:lib:latest");

        inspection(value("/configuration/dependencies[0]"))
                .expectHighlight("Version 'latest' can only be resolved by the Concord server. " +
                        "Add a concrete version to the 'cli' profile for IDE support.");
    }

    @Test
    void testLatestVersionCoveredByCliProfile() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:latest"
                profiles:
                  cli:
                    configuration:
                      dependencies:
                        - "mvn://com.example:lib:2.0.0"
                """);

        setSkipped("mvn://com.example:lib:latest");

        inspection(value("/configuration/dependencies[0]"))
                .expectNoProblems(HighlightSeverity.WEAK_WARNING);
    }

    @Test
    void testPlaceholderVersionShowsInfo() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:PROJECT_VERSION"
                """);

        setSkipped("mvn://com.example:lib:PROJECT_VERSION");

        inspection(value("/configuration/dependencies[0]"))
                .expectHighlight("Version 'PROJECT_VERSION' can only be resolved by the Concord server. " +
                        "Add a concrete version to the 'cli' profile for IDE support.");
    }

    @Test
    void testNormalVersionNotAffected() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                """);

        // Not in skipped deps — should produce no highlights from this inspection
        assertNoWarnings();
    }

    @Test
    void testNoSkippedDependencies() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:latest"
                """);

        // skippedDependencies is empty by default
        assertNoWarnings();
    }

    @Test
    void testCliProfileExtraDependenciesCoverage() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:latest"
                profiles:
                  cli:
                    configuration:
                      extraDependencies:
                        - "mvn://com.example:lib:3.0.0"
                """);

        setSkipped("mvn://com.example:lib:latest");

        inspection(value("/configuration/dependencies[0]"))
                .expectNoProblems(HighlightSeverity.WEAK_WARNING);
    }

    @Test
    void testSkippedInProfile() {
        configureFromText("""
                profiles:
                  myProfile:
                    configuration:
                      dependencies:
                        - "mvn://com.example:lib:latest"
                """);

        setSkipped("mvn://com.example:lib:latest");

        inspection(value("/profiles/myProfile/configuration/dependencies[0]"))
                .expectHighlight("Version 'latest' can only be resolved by the Concord server. " +
                        "Add a concrete version to the 'cli' profile for IDE support.");
    }

    private void setSkipped(String... coordinates) {
        Set<MavenCoordinate> skipped = new LinkedHashSet<>();
        for (var c : coordinates) {
            skipped.add(Objects.requireNonNull(MavenCoordinate.parse(c), "Invalid coordinate: " + c));
        }
        TaskRegistry.getInstance(getProject()).setSkippedDependencies(skipped);
    }
}
