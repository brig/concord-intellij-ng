package brig.concord.assertions;

import brig.concord.ConcordYamlPath;
import brig.concord.ConcordYamlTestBase;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Assertions;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class FlowDocAssertions {

    public static void assertFlowDoc(ConcordYamlPath yamlPath, ConcordYamlTestBase.KeyTarget flowKey, Consumer<FlowDocAssertions> assertions) {
        ReadAction.run(() -> {
            var kv = yamlPath.keyElement(flowKey.path()).getParent();

            var sibling = kv.getPrevSibling();
            while (sibling != null && !(sibling instanceof FlowDocumentation)) {
                sibling = sibling.getPrevSibling();
            }

            Assertions.assertInstanceOf(FlowDocumentation.class, sibling,
                    "FlowDocumentation not found before: " + flowKey.path() +
                            "\nPrev sibling was: " + (kv.getPrevSibling() == null ? "null" : kv.getPrevSibling().getClass().getName()) +
                            "\nKey element: " + kv.getText());
            assertions.accept(new FlowDocAssertions((FlowDocumentation) sibling));
        });
    }

    /**
     * Assert that the file contains a specific number of FlowDocumentation elements.
     */
    public static void assertFlowDocCount(PsiFile file, int expected) {
        var docs = PsiTreeUtil.findChildrenOfType(file, FlowDocumentation.class);
        assertEquals(expected, docs.size());
    }

    private final FlowDocumentation doc;

    public FlowDocAssertions(FlowDocumentation doc) {
        this.doc = doc;
    }

    public FlowDocAssertions hasFlowName(String expected) {
        assertEquals(expected, doc.getFlowName());
        return this;
    }

    public FlowDocAssertions hasDescription(String expected) {
        assertEquals(expected, doc.getDescription());
        return this;
    }

    public FlowDocAssertions noDescription() {
        assertNull(doc.getDescription());
        return this;
    }

    public FlowDocAssertions descriptionContains(String text) {
        var description = doc.getDescription();
        assertNotNull(description, "Description should not be null");
        assertTrue(description.contains(text),
                () -> "Description should contain: " + text + "\nActual:\n" + description);
        return this;
    }

    public FlowDocAssertions hasInputCount(int expected) {
        assertEquals(expected, doc.getInputParameters().size());
        return this;
    }

    public FlowDocAssertions hasOutputCount(int expected) {
        assertEquals(expected, doc.getOutputParameters().size());
        return this;
    }

    public ParamAssert param(String name) {
        var param = doc.findParameter(name);
        assertNotNull(param, "Parameter not found: " + name);
        return new ParamAssert(param, this);
    }

    public static class ParamAssert {

        private final FlowDocParameter param;
        private final FlowDocAssertions parent;

        ParamAssert(FlowDocParameter param, FlowDocAssertions parent) {
            this.param = param;
            this.parent = parent;
        }

        public ParamAssert hasType(String expected) {
            assertEquals(expected, param.getType());
            return this;
        }

        public ParamAssert hasBaseType(String expected) {
            assertEquals(expected, param.getBaseType());
            return this;
        }

        public ParamAssert hasDescription(String expected) {
            assertEquals(expected, param.getDescription());
            return this;
        }

        public ParamAssert isMandatory() {
            assertTrue(param.isMandatory(),
                    () -> "Parameter should be mandatory: " + param.getName());
            return this;
        }

        public ParamAssert isOptional() {
            assertFalse(param.isMandatory(),
                    () -> "Parameter should be optional: " + param.getName());
            return this;
        }

        public ParamAssert isArrayType() {
            assertTrue(param.isArrayType(),
                    () -> "Parameter should be array type: " + param.getName());
            return this;
        }

        public ParamAssert isInput() {
            assertTrue(param.isInputParameter(),
                    () -> "Expected input param: " + param.getName());
            assertFalse(param.isOutputParameter(),
                    () -> "Expected NOT output param: " + param.getName());
            return this;
        }

        public ParamAssert isOutput() {
            assertTrue(param.isOutputParameter(),
                    () -> "Expected output param: " + param.getName());
            assertFalse(param.isInputParameter(),
                    () -> "Expected NOT input param: " + param.getName());
            return this;
        }

        public FlowDocAssertions and() {
            return parent;
        }
    }
}
