package brig.concord.parser;

import brig.concord.ConcordYamlTestBase;
import brig.concord.assertions.FlowDocAssertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static brig.concord.assertions.FlowDocAssertions.assertFlowDocCount;

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
        var file = configureFromText(yaml);

        assertFlowDocCount(file, 0);
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
              #   booleanArray: boolean[], mandatory
              #   objectArray: object[], mandatory
              #   anyArray: any[], mandatory
              ##
              allTypes:
                - log: "test"
            """;
        configureFromText(yaml);

        flowDocFor(key("/flows/allTypes"), doc -> doc
                .hasInputCount(11)
                .hasFlowName("allTypes")
                .noDescription()
                .param("stringParam").hasType("string").and()
                .param("intParam").hasType("int").and()
                .param("numberParam").hasType("number").and()
                .param("boolParam").hasType("boolean").and()
                .param("objectParam").hasType("object").and()
                .param("anyParam").hasType("any").and()
                .param("stringArray").hasType("string[]").and()
                .param("intArray").hasType("int[]").and()
                .param("booleanArray").hasType("boolean[]").and()
                .param("objectArray").hasType("object[]").and()
                .param("anyArray").hasType("any[]"));
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

    private void flowDocFor(KeyTarget flowKey, Consumer<FlowDocAssertions> assertions) {
        FlowDocAssertions.assertFlowDoc(yamlPath, flowKey, assertions);
    }
}
