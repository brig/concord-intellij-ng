// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class InvalidResourcesPatternInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(InvalidResourcesPatternInspection.class);
    }

    @Test
    void testInvalidGlobPatternIsHighlighted() {
        configureFromText("""
                resources:
                  concord:
                    - "glob:["
                """);

        inspection(value("/resources/concord[0]"))
                .expectHighlight("Invalid resources pattern 'glob:['");
    }

    @Test
    void testInvalidRegexPatternIsHighlighted() {
        configureFromText("""
                resources:
                  concord:
                    - "regex:*invalid("
                """);

        inspection(value("/resources/concord[0]"))
                .expectHighlight("Invalid resources pattern 'regex:*invalid('");
    }

    @Test
    void testValidPatternsAreNotHighlighted() {
        configureFromText("""
                resources:
                  concord:
                    - "glob:flows/*.concord.yaml"
                    - "regex:.*/flows/.*\\.concord\\.yaml"
                    - "flows/main.concord.yaml"
                """);

        assertNoErrors();
    }

    @Test
    void testPatternLikeValuesOutsideResourcesAreIgnored() {
        configureFromText("""
                triggers:
                  github:
                    - conditions:
                        files:
                          - added:
                              - "glob:["
                """);

        assertNoErrors();
    }
}
