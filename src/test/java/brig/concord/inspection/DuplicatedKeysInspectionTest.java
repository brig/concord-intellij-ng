package brig.concord.inspection;

import brig.concord.yaml.psi.YAMLFile;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

public class DuplicatedKeysInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(DuplicatedKeysInspection.class);
    }

    @Test
    public void testDuplicatesInSameMapping() {
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
    public void testArrayItems() {
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
    public void testNoDuplicates() {
        configureFromText("""
            configuration:
              a: 1
              b: 2
              c: 3
            """);

        inspection(doc()).expectNoErrors();
    }

    @Test
    public void testSameKeyInDifferentMappings_isOk() {
        configureFromText("""
            configuration:
              a: 1
            flow:
              a: 2
            """);

        inspection(doc()).expectNoErrors();
    }

    @Test
    public void testDuplicatesInNestedMapping() {
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
    public void testDuplicatesInSequenceItemMapping() {
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
    public void testMultipleDuplicates_threeOccurrences() {
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
    public void testDuplicatesForMultipleKeys() {
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
    public void testQuotedAndUnquotedSameKey() {
        configureFromText("""
            configuration:
              "a": 1
              a: 2
            """);

        inspection(key("/configuration/a", 2))
                .expectDuplicateKey();
    }

    @Test
    public void testDoesNotRunOnNonConcordYaml() {
        var file = myFixture.configureByText(
                "test.yml",
                """
                configuration:
                  a: 1
                  a: 2
                """
        );
        assertFalse(file instanceof YAMLFile);

        inspection(doc()).expectNoErrors();
    }
}
