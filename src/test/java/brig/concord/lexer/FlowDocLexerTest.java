package brig.concord.lexer;

import org.junit.jupiter.api.Test;

import static brig.concord.assertions.TokenAssertions.assertTokens;

public class FlowDocLexerTest {

    @Test
    public void testFlowDocTokens() {
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

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("FLOW_DOC_COMMENT_PREFIX", 5);
    }

    @Test
    public void testMultipleFlowDocs() {
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

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 4);
    }

    @Test
    public void testCustomTagsInFlowDoc() {
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

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("FLOW_DOC_PARAM_NAME", 1)
                .token("FLOW_DOC_PARAM_NAME").hasText("s3Bucket");
    }

    @Test
    public void testFlowDocWithEmptyLines() {
        var yaml = """
            flows:
              ##
              # Process S3 files
              #
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              #
              # out:
              #   processed: int, mandatory, Files processed count
              #
              # tags: 1, 2, 3
              ##
              processS3:
                - task: s3
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }

    @Test
    public void testCommentPrefixIsSeparateToken() {
        var yaml = """
            flows:
              ##
              # Description
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_COMMENT_PREFIX", 1)
                .token("FLOW_DOC_COMMENT_PREFIX").hasText("#").followedBy("whitespace")
                .and()
                .token("FLOW_DOC_CONTENT").hasText("Description");
    }

    @Test
    public void testHashWithoutSpaceInDescription() {
        var yaml = """
            flows:
              ##
              #NoSpace
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("FLOW_DOC_COMMENT_PREFIX", 1)
                .token("FLOW_DOC_CONTENT").hasText("NoSpace");
    }

    @Test
    public void testHashWithoutSpaceInParams() {
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string, mandatory
              #NoSpaceTag
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("FLOW_DOC_PARAM_NAME", 1)
                .token("FLOW_DOC_PARAM_NAME").hasText("param1")
                .and()
                .token("FLOW_DOC_TEXT").hasText("NoSpaceTag");
    }

    @Test
    public void testMandatoryAndOptionalTokens() {
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string, mandatory, Required param
              #   param2: int, optional, Optional param
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MANDATORY", 1)
                .hasCount("FLOW_DOC_OPTIONAL", 1)
                .token("FLOW_DOC_MANDATORY").hasText("mandatory")
                .and()
                .token("FLOW_DOC_OPTIONAL").hasText("optional");
    }

    @Test
    public void testEmptyCommentLine() {
        var yaml = """
            flows:
              ##
              # Description
              #
              # in:
              #   param1: string, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_COMMENT_PREFIX", 4)
                .hasCount("FLOW_DOC_MARKER", 2);
    }

    @Test
    public void testColonAndCommaTokens() {
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string, mandatory, Description
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_COLON", 1)
                .hasCount("FLOW_DOC_COMMA", 2)
                .token("FLOW_DOC_COLON").hasText(":")
                .and()
                .token("FLOW_DOC_COMMA", 0).hasText(",")
                .and()
                .token("FLOW_DOC_COMMA", 1).hasText(",");
    }

    @Test
    public void testArbitraryIndentation() {
        // Section headers can have any indentation, parameters must have greater indent
        var yaml = """
            flows:
              ##
              #     in:
              #       param1: string, mandatory
              #       param2: int, optional
              #     out:
              #       result: boolean, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("FLOW_DOC_SECTION_HEADER", 2)
                .hasCount("FLOW_DOC_PARAM_NAME", 3)
                .token("FLOW_DOC_PARAM_NAME", 0).hasText("param1")
                .and()
                .token("FLOW_DOC_PARAM_NAME", 1).hasText("param2")
                .and()
                .token("FLOW_DOC_PARAM_NAME", 2).hasText("result");
    }

    @Test
    public void testUserTagWithSameIndentAsSection() {
        // User tag with same indent as section should NOT be treated as parameter
        var yaml = """
            flows:
              ##
              #   in:
              #     param1: string, mandatory
              #   tags: internal
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_PARAM_NAME", 1)
                .token("FLOW_DOC_PARAM_NAME").hasText("param1")
                .and()
                .token("FLOW_DOC_TEXT").hasText("tags: internal");
    }

    @Test
    public void testUserTagWithSmallerIndentThanSection() {
        // User tag with smaller indent than section should NOT be treated as parameter
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

        assertTokens(yaml)
                .hasCount("FLOW_DOC_PARAM_NAME", 1)
                .token("FLOW_DOC_PARAM_NAME").hasText("param1")
                .and()
                .token("FLOW_DOC_TEXT").hasText("tags: value");
    }

    @Test
    public void testNestedSectionIsNotRecognized() {
        // out: with greater indent than in: is nested, NOT a new section
        // All nested content becomes parameters of the parent section
        var yaml = """
            flows:
              ##
              # in:
              #   inputParam: string, mandatory
              #     out:
              #       outputParam: int, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        // Only 1 section header (in:), out: is nested so treated as param name
        // outputParam also becomes a param in the in: section
        assertTokens(yaml)
                .hasCount("FLOW_DOC_SECTION_HEADER", 1)
                .hasCount("FLOW_DOC_PARAM_NAME", 3)
                .token("FLOW_DOC_SECTION_HEADER").hasText("in:")
                .and()
                .token("FLOW_DOC_PARAM_NAME", 0).hasText("inputParam")
                .and()
                .token("FLOW_DOC_PARAM_NAME", 1).hasText("out")
                .and()
                .token("FLOW_DOC_PARAM_NAME", 2).hasText("outputParam");
    }

    @Test
    public void testSameIndentSectionsRecognized() {
        // in: and out: with same indent are both sections
        var yaml = """
            flows:
              ##
              #   in:
              #     inputParam: string, mandatory
              #   out:
              #     outputParam: int, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_SECTION_HEADER", 2)
                .hasCount("FLOW_DOC_PARAM_NAME", 2)
                .token("FLOW_DOC_PARAM_NAME", 0).hasText("inputParam")
                .and()
                .token("FLOW_DOC_PARAM_NAME", 1).hasText("outputParam");
    }
}
