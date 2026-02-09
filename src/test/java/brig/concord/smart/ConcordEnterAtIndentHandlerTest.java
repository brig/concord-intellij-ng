package brig.concord.smart;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.actionSystem.IdeActions;
import org.junit.jupiter.api.Test;

class ConcordEnterAtIndentHandlerTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testMapping() {
        configureFromText("""
                flows:<caret>
                """);

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        myFixture.checkResult("""
                flows:
                  <caret>
                """);
    }

    @Test
    void testSequenceContinuation() {
        configureFromText("""
                flows:
                  default:
                    - log: "step 1"<caret>
                """);

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        myFixture.checkResult("""
                flows:
                  default:
                    - log: "step 1"
                      <caret>
                """);
    }

    @Test
    void testNestedSequenceContinuation() {
        configureFromText("""
                flows:
                  default:
                    - if: ${true}
                      then:
                        - log: "nested 1"<caret>
                """);

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        myFixture.checkResult("""
                flows:
                  default:
                    - if: ${true}
                      then:
                        - log: "nested 1"
                          <caret>
                """);
    }

    @Test
    void testFlowDocumentation() {
        configureFromText("""
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.<caret>
              # in:
              #   file: string, mandatory, k8s manifest file to delete
              ##
              default:
                - log: "Hello"
            """);

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        myFixture.checkResult("""
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.
              # <caret>
              # in:
              #   file: string, mandatory, k8s manifest file to delete
              ##
              default:
                - log: "Hello"
            """);
    }

    @Test
    void testFlowDocumentationParam() {
        configureFromText("""
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.
              # in:
              #   file: string, mandatory, k8s manifest file to delete<caret>
              ##
              default:
                - log: "Hello"
            """);

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        myFixture.checkResult("""
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.
              # in:
              #   file: string, mandatory, k8s manifest file to delete
              #   <caret>
              ##
              default:
                - log: "Hello"
            """);
    }
}
