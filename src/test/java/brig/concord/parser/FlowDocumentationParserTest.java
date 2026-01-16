package brig.concord.parser;

import brig.concord.ConcordYamlTestBase;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class FlowDocumentationParserTest extends ConcordYamlTestBase {

    @Test
    public void testBasicFlowDocumentation() {
        var yaml = """
            flows:
              ##
              # Process S3 files
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              # out:
              #   processed: int, mandatory, Files processed count
              ##
              processS3:
                - task: s3
            """;

        configureFromText(yaml);

        flowDocFor(key("/flows/processS3"), doc -> doc
                .hasFlowName("processS3")
                .hasDescription("Process S3 files")
                .hasInputCount(1)
                .hasOutputCount(1)
                .param("s3Bucket").hasType("string").isMandatory().hasDescription("S3 bucket name").and()
                .param("processed").hasType("int").isMandatory());
    }

    @Test
    public void testBasicFlowDocumentationWithExtraComments() {
        var yaml = """
            flows:
              ##
              # Process S3 files
              # and
              # something
              #
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              #
              # out:
              #   processed: int, mandatory, Files processed count
              #
              ##
              processS3:
                - task: s3
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/processS3"), doc -> doc
                .hasFlowName("processS3")
                .hasDescription("Process S3 files\nand\nsomething")
                .hasInputCount(1)
                .hasOutputCount(1)
                .param("s3Bucket").hasType("string").isMandatory().hasDescription("S3 bucket name").and()
                .param("processed").hasType("int").isMandatory());
    }

    @Test
    public void testArrayTypes() {
        var yaml = """
            flows:
              ##
              # in:
              #   files: string[], mandatory, File paths
              # out:
              #   results: boolean[], mandatory, Success flags
              ##
              processFiles:
                - log: "test"
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/processFiles"), doc -> doc
                .param("files").hasType("string[]").isArrayType().hasBaseType("string").and()
                .param("results").hasType("boolean[]").isArrayType().hasBaseType("boolean"));
    }

    @Test
    public void testNestedObjectParameters() {
        var yaml = """
            flows:
              ##
              # in:
              #   config: object, mandatory, Configuration
              #   config.host: string, mandatory, Server host
              #   config.port: int, optional, Server port
              ##
              connect:
                - task: connect
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/connect"), doc -> doc
                .hasInputCount(3)
                .param("config").hasType("object").and()
                .param("config.host").hasType("string").isMandatory().and()
                .param("config.port").hasType("int").isOptional());
    }

    @Test
    public void testMultilineDescription() {
        var yaml = """
            flows:
              ##
              # Process S3 files and upload results.
              # This is a multiline description that
              # spans multiple lines.
              #
              # in:
              #   bucket: string, mandatory
              ##
              process:
                - log: "test"
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/process"), doc -> doc
                .descriptionContains("Process S3 files")
                .descriptionContains("multiline description")
                .descriptionContains("spans multiple lines"));
    }

    @Test
    public void testFlowWithoutDocumentation() {
        var yaml = """
            flows:
              undocumentedFlow:
                - log: "test"
            """;

        assertFlowDocCount(yaml, 0);
    }

    @Test
    public void testMultipleFlows() {
        var yaml = """
            flows:
              ##
              # First flow
              # in:
              #   param1: string, mandatory
              ##
              flow1:
                - log: "1"

              ##
              # Second flow
              # in:
              #   param2: int, optional
              ##
              flow2:
                - log: "2"
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/flow1"), doc -> doc
                .hasDescription("First flow"));

        flowDocFor(key("/flows/flow2"), doc -> doc
                .hasDescription("Second flow"));
    }

    @Test
    public void testAllTypes() {
        var yaml = """
            flows:
              ##
              # in:
              #   stringParam: string, mandatory
              #   intParam: int, mandatory
              #   numberParam: number, mandatory
              #   boolParam: boolean, mandatory
              #   objectParam: object, mandatory
              #   anyParam: any, mandatory
              #   stringArray: string[], mandatory
              #   intArray: int[], mandatory
              ##
              allTypes:
                - log: "test"
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/allTypes"), doc -> doc
                .hasInputCount(8)
                .hasFlowName("allTypes")
                .noDescription()
                .param("stringParam").hasType("string").and()
                .param("intParam").hasType("int").and()
                .param("numberParam").hasType("number").and()
                .param("boolParam").hasType("boolean").and()
                .param("objectParam").hasType("object").and()
                .param("anyParam").hasType("any").and()
                .param("stringArray").hasType("string[]").and()
                .param("intArray").hasType("int[]"));
    }

    @Test
    public void testParameterDirection() {
        var yaml = """
            flows:
              ##
              # in:
              #   input1: string, mandatory
              # out:
              #   output1: string, mandatory
              ##
              testFlow:
                - log: "test"
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/testFlow"), doc -> doc
                .param("input1").isInput().and()
                .param("output1").isOutput());
    }

    private void flowDocFor(KeyTarget flowKey, Consumer<FlowDocAssert> assertions) {
        ReadAction.run(() -> {
            var kv = yamlPath.keyElement(flowKey.path()).getParent();

            // FlowDocumentation is the previous sibling of the flow key-value
            var sibling = kv.getPrevSibling();
            while (sibling != null && !(sibling instanceof FlowDocumentation)) {
                sibling = sibling.getPrevSibling();
            }

            Assertions.assertInstanceOf(FlowDocumentation.class, sibling,
                    "FlowDocumentation not found before: " + flowKey.path() +
                            "\nPrev sibling was: " + (kv.getPrevSibling() == null ? "null" : kv.getPrevSibling().getClass().getName()) +
                            "\nKey element: " + kv.getText());
            assertions.accept(new FlowDocAssert((FlowDocumentation) sibling));
        });
    }

    private void assertFlowDocCount(String yaml, int expected) {
        var file = configureFromText(yaml);
        var docs = PsiTreeUtil.findChildrenOfType(file, FlowDocumentation.class);
        Assertions.assertEquals(expected, docs.size());
    }

    static class FlowDocAssert {
        private final FlowDocumentation doc;

        FlowDocAssert(FlowDocumentation doc) {
            this.doc = doc;
        }

        FlowDocAssert hasFlowName(String expected) {
            Assertions.assertEquals(expected, doc.getFlowName());
            return this;
        }

        FlowDocAssert hasDescription(String expected) {
            Assertions.assertEquals(expected, doc.getDescription());
            return this;
        }

        FlowDocAssert noDescription() {
            Assertions.assertNull(doc.getDescription());
            return this;
        }
        FlowDocAssert descriptionContains(String text) {
            var description = doc.getDescription();
            assertNotNull(description);
            Assertions.assertTrue(description.contains(text),
                    () -> "Description should contain: " + text + "\nActual:\n" + description);
            return this;
        }

        FlowDocAssert hasInputCount(int expected) {
            Assertions.assertEquals(expected, doc.getInputParameters().size());
            return this;
        }

        FlowDocAssert hasOutputCount(int expected) {
            Assertions.assertEquals(expected, doc.getOutputParameters().size());
            return this;
        }

        ParamAssert param(String name) {
            var param = doc.findParameter(name);
            Assertions.assertNotNull(param, "Parameter not found: " + name);
            return new ParamAssert(param, this);
        }
    }

    static class ParamAssert {
        private final FlowDocParameter param;
        private final FlowDocAssert parent;

        ParamAssert(FlowDocParameter param, FlowDocAssert parent) {
            this.param = param;
            this.parent = parent;
        }

        ParamAssert hasType(String expected) {
            Assertions.assertEquals(expected, param.getType());
            return this;
        }

        ParamAssert hasBaseType(String expected) {
            Assertions.assertEquals(expected, param.getBaseType());
            return this;
        }

        ParamAssert hasDescription(String expected) {
            Assertions.assertEquals(expected, param.getDescription());
            return this;
        }

        ParamAssert isMandatory() {
            Assertions.assertTrue(param.isMandatory(), "Parameter should be mandatory: " + param.getName());
            return this;
        }

        ParamAssert isOptional() {
            Assertions.assertFalse(param.isMandatory(), "Parameter should be optional: " + param.getName());
            return this;
        }

        ParamAssert isArrayType() {
            Assertions.assertTrue(param.isArrayType(), "Parameter should be array type: " + param.getName());
            return this;
        }

        ParamAssert isInput() {
            Assertions.assertTrue(param.isInputParameter(),
                    () -> "Expected input param, but was not: " + param.getName());
            Assertions.assertFalse(param.isOutputParameter(),
                    () -> "Expected NOT output param: " + param.getName());

            return this;
        }

        ParamAssert isOutput() {
            Assertions.assertTrue(param.isOutputParameter(),
                    () -> "Expected oiutput param, but was not: " + param.getName());
            Assertions.assertFalse(param.isInputParameter(),
                    () -> "Expected NOT input param: " + param.getName());

            return this;
        }

        FlowDocAssert and() {
            return parent;
        }
    }
}
