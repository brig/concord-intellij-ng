package brig.concord.editor;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.ui.components.breadcrumbs.Crumb;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ConcordBreadcrumbsProviderTest extends ConcordYamlTestBase {

    @Test
    public void testFlowDefinition() {
        configureFromText("""
                flows:
                  def<caret>ault:
                    - log: "Hello"
                """);

        assertBreadCrumbs("default");
    }

    @Test
    public void testStepWithName() {
        configureFromText("""
                flows:
                  default:
                    - name: My Step
                      lo<caret>g: "Hello"
                """);

        assertBreadCrumbs("default", "My Step");
    }

    @Test
    public void testTaskStep() {
        configureFromText("""
                flows:
                  default:
                    - task: my<caret>Task
                """);

        assertBreadCrumbs("default", "task: myTask");
    }

    @Test
    public void testCallStep() {
        configureFromText("""
                flows:
                  default:
                    - ca<caret>ll: otherFlow
                """);

        assertBreadCrumbs("default", "call: otherFlow");
    }

    @Test
    public void testReturnStep() {
        configureFromText("""
                flows:
                  default:
                    - re<caret>turn
                """);

        assertBreadCrumbs("default", "return");
    }

    @Test
    public void testLongTextTruncation() {
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
