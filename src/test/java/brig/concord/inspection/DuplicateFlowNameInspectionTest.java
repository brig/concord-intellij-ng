package brig.concord.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class DuplicateFlowNameInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(DuplicateFlowNameInspection.class);
    }

    @Test
    void testDuplicateFlowInAnotherFile() {
        createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Main"
                """);

        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  main:
                    - log: "Utils"
                """);

        configureFromExistingFile(utilsFile);

        inspection(key("/flows/main"))
                .expectDuplicateFlowName("main", "concord.yaml");
    }

    @Test
    void testNoDuplicateInSameScope() {
        createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Main"
                """);

        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils"
                """);

        configureFromExistingFile(utilsFile);

        inspection(key("/flows/utilsFlow")).expectNoWarnings();
    }

    @Test
    void testDuplicateFlowInDifferentScopes_noWarning() {
        // Project A
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  sharedFlow:
                    - log: "Project A"
                """);

        // Project B - same flow name but different scope
        var rootB = createFile("project-b/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  sharedFlow:
                    - log: "Project B"
                """);

        // Project A
        configureFromExistingFile(rootA);

        // No warning because they are in different scopes
        inspection(key("/flows/sharedFlow")).expectNoWarnings();

        // Project B
        configureFromExistingFile(rootB);

        // No warning because they are in different scopes
        inspection(key("/flows/sharedFlow")).expectNoWarnings();
    }

    @Test
    void testMultipleDuplicates_reportsFirst() {
        createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  myFlow:
                    - log: "Root"
                """);

        createFile("project-a/concord/a.concord.yaml", """
                flows:
                  myFlow:
                    - log: "A"
                """);

        var fileC = createFile("project-a/concord/b.concord.yaml", """
                flows:
                  myFlow:
                    - log: "B"
                """);

        configureFromExistingFile(fileC);

        // Should report at least one duplicate (the first found)
        inspection(key("/flows/myFlow"))
                .expectHighlight("Flow 'myFlow' is also defined in 'concord.yaml'");
    }

    @Test
    void testSingleFlowDefinition_noWarning() {
        configureFromText("""
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Hello"
                  helper:
                    - log: "Helper"
                """);

        inspection(key("/flows/main")).expectNoWarnings();
        inspection(key("/flows/helper")).expectNoWarnings();
    }
}
