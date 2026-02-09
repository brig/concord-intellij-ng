package brig.concord.inspection;

import brig.concord.yaml.psi.YAMLFile;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class DuplicatedKeysInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(DuplicatedKeysInspection.class);
    }

    @Test
    void testDuplicatesInSameMapping() {
        configureFromText("""
                configuration:
                  a: 1
                  b: 2
                  a: 3
                """);

        inspection(key("/configuration/a", 2))
                .expectDuplicateKey();
    }

    @Test
    void testArrayItems() {
        configureFromText(
                """
                        flows:
                          myFlow:
                            - log: "First"
                            - log: "Second"
                        """
        );

        inspection(doc()).expectNoErrors();
    }

    @Test
    void testNoDuplicates() {
        configureFromText("""
            configuration:
              a: 1
              b: 2
              c: 3
            """);

        inspection(doc()).expectNoErrors();
    }

    @Test
    void testSameKeyInDifferentMappings_isOk() {
        configureFromText("""
            configuration:
              a: 1
            flow:
              a: 2
            """);

        inspection(doc()).expectNoErrors();
    }

    @Test
    void testDuplicatesInNestedMapping() {
        configureFromText("""
            configuration:
              nested:
                x: 1
                x: 2
            """);

        inspection(key("/configuration/nested/x", 2))
                .expectDuplicateKey();
    }

    @Test
    void testDuplicatesInSequenceItemMapping() {
        configureFromText("""
            profiles:
              - configuration:
                  a: 1
                  a: 2
            """);

        inspection(key("/profiles[0]/configuration/a", 2))
                .expectDuplicateKey();
    }

    @Test
    void testMultipleDuplicates_threeOccurrences() {
        configureFromText("""
            configuration:
              a: 1
              a: 2
              a: 3
            """);

        inspection(key("/configuration/a", 1)).expectNoErrors();
        inspection(key("/configuration/a", 2)).expectDuplicateKey();
        inspection(key("/configuration/a", 3)).expectDuplicateKey();
    }

    @Test
    void testDuplicatesForMultipleKeys() {
        configureFromText("""
            configuration:
              a: 1
              b: 1
              a: 2
              b: 2
            """);

        inspection(key("/configuration/a", 2)).expectDuplicateKey();
        inspection(key("/configuration/b", 2)).expectDuplicateKey();
    }

    @Test
    void testQuotedAndUnquotedSameKey() {
        configureFromText("""
            configuration:
              "a": 1
              a: 2
            """);

        inspection(key("/configuration/a", 2))
                .expectDuplicateKey();
    }

    @Test
    void testDoesNotRunOnNonConcordYaml() {
        var file = myFixture.configureByText(
                "test.yml",
                """
                configuration:
                  a: 1
                  a: 2
                """
        );
        Assertions.assertFalse(file instanceof YAMLFile);

        inspection(doc()).expectNoErrors();
    }
}
