// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.parser;

import brig.concord.ConcordYamlTestBaseJunit5;
import org.junit.jupiter.api.Test;

import static brig.concord.assertions.FlowDocAssertions.assertFlowDocCount;

class FlowDocumentationParserTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testBasicFlowDocumentation() {
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

        assertFlowDoc(key("/flows/processS3"), doc -> doc
                .hasFlowName("processS3")
                .hasDescription("Process S3 files")
                .hasInputCount(1)
                .hasOutputCount(1)
                .param("s3Bucket").hasType("string").isMandatory().hasDescription("S3 bucket name").and()
                .param("processed").hasType("int").isMandatory());
    }

    @Test
    void testBasicFlowDocumentationWithArbitraryIndent() {
        // Arbitrary indentation is allowed, but sections must be at same or lesser indent level
        var yaml = """
            flows:
              ##
              #     Process S3 files
              #   in:
              #      s3Bucket: string, mandatory, S3 bucket name
              #   out:
              #      processed: int, mandatory, Files processed count
              ##
              processS3:
                - task: s3
            """;

        configureFromText(yaml);

        assertFlowDoc(key("/flows/processS3"), doc -> doc
                .hasFlowName("processS3")
                .hasDescription("Process S3 files")
                .hasInputCount(1)
                .hasOutputCount(1)
                .param("s3Bucket").hasType("string").isMandatory().hasDescription("S3 bucket name").and()
                .param("processed").hasType("int").isMandatory());
    }

    @Test
    void testBasicFlowDocumentationWithExtraComments() {
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

        assertFlowDoc(key("/flows/processS3"), doc -> doc
                .hasFlowName("processS3")
                .hasDescription("Process S3 files\nand\nsomething")
                .hasInputCount(1)
                .hasOutputCount(1)
                .param("s3Bucket").hasType("string").isMandatory().hasDescription("S3 bucket name").and()
                .param("processed").hasType("int").isMandatory());
    }

    @Test
    void testArrayTypes() {
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

        assertFlowDoc(key("/flows/processFiles"), doc -> doc
                .param("files").hasType("string[]").isArrayType().hasBaseType("string").and()
                .param("results").hasType("boolean[]").isArrayType().hasBaseType("boolean"));
    }

    @Test
    void testNestedObjectParameters() {
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

        assertFlowDoc(key("/flows/connect"), doc -> doc
                .hasInputCount(3)
                .param("config").hasType("object").and()
                .param("config.host").hasType("string").isMandatory().and()
                .param("config.port").hasType("int").isOptional());
    }

    @Test
    void testMultilineDescription() {
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

        assertFlowDoc(key("/flows/process"), doc -> doc
                .descriptionContains("Process S3 files")
                .descriptionContains("multiline description")
                .descriptionContains("spans multiple lines"));
    }

    @Test
    void testFlowWithoutDocumentation() {
        var yaml = """
            flows:
              undocumentedFlow:
                - log: "test"
            """;
        var file = configureFromText(yaml);

        assertFlowDocCount(file, 0);
    }

    @Test
    void testMultipleFlows() {
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

        assertFlowDoc(key("/flows/flow1"), doc -> doc
                .hasDescription("First flow"));

        assertFlowDoc(key("/flows/flow2"), doc -> doc
                .hasDescription("Second flow"));
    }

    @Test
    void testAllTypes() {
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

        assertFlowDoc(key("/flows/allTypes"), doc -> doc
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
    void testParameterDirection() {
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

        assertFlowDoc(key("/flows/testFlow"), doc -> doc
                .param("input1").isInput().and()
                .param("output1").isOutput());
    }

    @Test
    void testCustomTagsInFlowDocumentation() {
        var yaml = """
            flows:
              ##
              # Process S3 files
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              # tags: not in param, just user tag
              ##
              processS3:
                - task: s3
            """;

        configureFromText(yaml);

        assertFlowDoc(key("/flows/processS3"), doc -> doc
                .hasFlowName("processS3")
                .hasDescription("Process S3 files")
                .hasInputCount(1)
                .hasOutputCount(0)
                .param("s3Bucket").hasType("string").isMandatory().hasDescription("S3 bucket name"));
    }

    @Test
    void testArbitraryIndentationInSections() {
        var yaml = """
            flows:
              ##
              #     in:
              #       bucket: string, mandatory, S3 bucket name
              #       prefix: string, optional, Filter prefix
              #     out:
              #       count: int, mandatory, Processed count
              ##
              processS3:
                - task: s3
            """;

        configureFromText(yaml);

        assertFlowDoc(key("/flows/processS3"), doc -> doc
                .hasFlowName("processS3")
                .hasInputCount(2)
                .hasOutputCount(1)
                .param("bucket").hasType("string").isMandatory().and()
                .param("prefix").hasType("string").isOptional().and()
                .param("count").hasType("int").isMandatory());
    }

    @Test
    void testUserTagWithSameIndentAsSection() {
        // tags: has same indent as in: so should NOT be a parameter
        var yaml = """
            flows:
              ##
              #   in:
              #     param1: string, mandatory
              #   tags: internal, deprecated
              #   see: docs/readme.md
              ##
              myFlow:
                - log: "test"
            """;

        configureFromText(yaml);

        assertFlowDoc(key("/flows/myFlow"), doc -> doc
                .hasInputCount(1)
                .hasOutputCount(0)
                .param("param1").hasType("string").isMandatory());
    }

    @Test
    void testUserTagWithSmallerIndentThanSection() {
        // tags: has smaller indent than in: so should NOT be a parameter
        var yaml = """
            flows:
              ##
              #     in:
              #       param1: string, mandatory
              # tags: value
              ##
              myFlow:
                - log: "test"
            """;

        configureFromText(yaml);

        assertFlowDoc(key("/flows/myFlow"), doc -> doc
                .hasInputCount(1)
                .hasOutputCount(0)
                .param("param1").hasType("string").isMandatory());
    }

    @Test
    void testNestedOutIsNotSection() {
        // out: with greater indent than in: is nested, so NOT a new section
        // Everything nested belongs to the parent section
        var yaml = """
            flows:
              ##
              # in:
              #   input1: string, mandatory
              #     out:
              #       output1: int, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        configureFromText(yaml);

        // out: is nested in in:, so both out and output1 become input parameters
        assertFlowDoc(key("/flows/myFlow"), doc -> doc
                .hasInputCount(3)
                .hasOutputCount(0)
                .param("input1").hasType("string").isInput().and()
                .param("out").isInput().and()
                .param("output1").hasType("int").isInput());
    }

    @Test
    void testSameIndentForInAndOut() {
        // in: and out: with same indent are both sections
        var yaml = """
            flows:
              ##
              #   in:
              #     input1: string, mandatory
              #   out:
              #     output1: int, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        configureFromText(yaml);

        assertFlowDoc(key("/flows/myFlow"), doc -> doc
                .hasInputCount(1)
                .hasOutputCount(1)
                .param("input1").hasType("string").isInput().and()
                .param("output1").hasType("int").isOutput());
    }

    @Test
    void testRequiredAsAliasForMandatory() {
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string, required, This param uses required
              #   param2: string, mandatory, This param uses mandatory
              ##
              myFlow:
                - log: "test"
            """;

        configureFromText(yaml);

        assertFlowDoc(key("/flows/myFlow"), doc -> doc
                .hasInputCount(2)
                .param("param1").hasType("string").isMandatory().and()
                .param("param2").hasType("string").isMandatory());
    }

}
