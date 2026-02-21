// SPDX-License-Identifier: Apache-2.0
package brig.concord.editor;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import com.intellij.ui.components.breadcrumbs.Crumb;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConcordBreadcrumbsProviderTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testFlowDefinition() {
        configureFromText("""
                flows:
                  def<caret>ault:
                    - log: "Hello"
                """);

        assertBreadCrumbs("default");
    }

    @Test
    void testStepWithName() {
        configureFromText("""
                flows:
                  default:
                    - name: My Step
                      lo<caret>g: "Hello"
                """);

        assertBreadCrumbs("default", "My Step");
    }

    @Test
    void testTaskStep() {
        configureFromText("""
                flows:
                  default:
                    - task: my<caret>Task
                """);

        assertBreadCrumbs("default", "task: myTask");
    }

    @Test
    void testCallStep() {
        configureFromText("""
                flows:
                  default:
                    - ca<caret>ll: otherFlow
                """);

        assertBreadCrumbs("default", "call: otherFlow");
    }

    @Test
    void testReturnStep() {
        configureFromText("""
                flows:
                  default:
                    - re<caret>turn
                """);

        assertBreadCrumbs("default", "return");
    }

    @Test
    void testLongTextTruncation() {
        configureFromText("""
                flows:
                  default:
                    - name: This is a very long name that should be truncated
                      l<caret>og: "Hello"
                """);

        assertBreadCrumbs("default", "This is a very long name that ...");
    }

    private void assertBreadCrumbs(String... elements) {
        ReadAction.run(() -> {
            var crumbs = myFixture.getBreadcrumbsAtCaret();
            Assertions.assertEquals(List.of(elements), crumbs.stream().map(Crumb::getText).toList());
        });
    }
}
