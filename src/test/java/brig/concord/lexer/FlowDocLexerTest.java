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
    public void testRequiredAsAliasForMandatory() {
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string, required, This is required
              #   param2: string, mandatory, This is mandatory
              ##
              myFlow:
                - log: "test"
            """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MANDATORY", 2)
                .token("FLOW_DOC_MANDATORY", 0).hasText("required")
                .and()
                .token("FLOW_DOC_MANDATORY", 1).hasText("mandatory");
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

    @Test
    public void testCommentsUnderConfiguration() {
        var yaml = """
                # ---------------------------------------------------------------------
                ## Github Create Repository
                ## --------------------------------------------------------------------
                ## Secrets:
                ##
                ##  - name: githubTokenSecret
                configuration:
                    arguments:
                        key: value
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0);
    }

    @Test
    public void testCommentsUnderFlowName() {
        var yaml = """
                flows:
                    # ---------------------------------------------------------------------
                    ## Github Create Repository
                    ## --------------------------------------------------------------------
                    ## Secrets:
                    ##
                    ##  - name: githubTokenSecret
                    default:
                        - log: "ME"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 1);  // Only opening ##, no closing marker
    }

    @Test
    public void testTripleHashInsideFlowDoc() {
        // ### inside flow doc should be treated as # (comment prefix) + ## (content)
        var yaml = """
                flows:
                  ##
                  # Description
                  ### This has triple hash
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("FLOW_DOC_COMMENT_PREFIX", 2)  // # from "# Description" and # from "###..."
                .hasToken("FLOW_DOC_CONTENT");
    }

    @Test
    public void testFlowDocWithYamlLikeContent() {
        // Content that looks like YAML should NOT be parsed as YAML
        var yaml = """
                flows:
                  ##
                  # in:
                  #   - item1
                  #   - item2
                  #   key: value
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("-", 1)  // Only the actual YAML sequence marker, not the ones in comments
                .hasCount("scalar key", 3);  // flows, myFlow, log - NOT "key" from comment
    }

    @Test
    public void testFlowDocMarkerWithTabs() {
        // ## followed by tabs should still close flow doc
        var yaml = "flows:\n  ##\n  # desc\n  ##\t\n  myFlow:\n    - log: test\n";

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }

    @Test
    public void testFlowDocMarkerFollowedBySpacesAndContent() {
        // ##   content should NOT be a closing marker
        var yaml = """
                flows:
                  ##
                  ##   This has spaces then content
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)  // open and close, middle line is content
                .tokenHasText("FLOW_DOC_CONTENT", "#   This has spaces then content");
    }

    @Test
    public void testEmptyFlowDoc() {
        // Just ## ## with nothing in between
        var yaml = """
                flows:
                  ##
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }

    @Test
    public void testFlowDocAtEndOfFile() {
        // Flow doc at EOF without closing marker
        var yaml = """
                flows:
                  ##
                  # description
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 1)  // Only opening, closes implicitly
                .hasToken("scalar key");  // myFlow should still be parsed
    }

    @Test
    public void testFlowDocAtEndOfFile2() {
        // Flow doc at EOF without closing marker
        var yaml = """
                flows:
                  ##
                  # description
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 1);  // myFlow should still be parsed
    }

    @Test
    public void testFlowDocOutsideFlowsSection() {
        // ## outside flows section should be regular comment
        var yaml = """
                configuration:
                  ##
                  ## This is just a comment
                  ##
                  runtime: concord-v2
                flows:
                  default:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0)  // No flow doc markers
                .hasCount("comment", 3);  // All ## lines are comments
    }

    @Test
    public void testFlowDocWithColonInDescription() {
        // Colons in description should not break parsing
        var yaml = """
                flows:
                  ##
                  # Note: this has a colon
                  # in:
                  #   param: string, mandatory, Value with: colons: inside
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasToken("FLOW_DOC_PARAM_NAME")
                .hasToken("FLOW_DOC_TEXT");  // description with colons
    }

    @Test
    public void testConsecutiveFlowDocs() {
        // Two flow docs back to back
        var yaml = """
                flows:
                  ##
                  # First doc
                  ##
                  ##
                  # Second doc
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 4);  // 2 open + 2 close
    }

    @Test
    public void testFlowDocWithOnlyMarkers() {
        // Multiple ## markers in sequence
        var yaml = """
                flows:
                  ##
                  ##
                  ##
                  ##
                  myFlow:
                    - log: "test"
                """;

        // First ## opens, second ## closes, third ## opens, fourth ## closes
        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 4);
    }

    @Test
    public void testHashHashHashHash() {
        // #### - should be # (prefix) + ### (content) when inside flow doc
        var yaml = """
                flows:
                  ##
                  ####
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }

    @Test
    public void testFlowDocInsideQuotedString() {
        // ## inside quoted string should NOT trigger flow doc
        var yaml = """
                flows:
                  default:
                    - log: "## This is not a marker ##"
                    - set:
                        msg: '##'
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0)
                .hasToken("scalar dstring")
                .hasToken("scalar string");
    }

    @Test
    public void testFlowDocWithDashDashContent() {
        // ##---- should be treated as # + #---- content, not as closing marker
        var yaml = """
                flows:
                  ##
                  ##----
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)  // open and close
                .hasToken("FLOW_DOC_CONTENT");  // the ##---- line becomes content
    }

    @Test
    public void testTwoFlowsWithHashHashComments() {
        // ## with content is just a comment, not a flow doc marker
        // Both flow names should be parsed correctly
        var yaml = """
                flows:
                  ## First flow description
                  firstFlow:
                    - log: "first"
                  ## Second flow description
                  secondFlow:
                    - log: "second"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0)  // No flow doc - ## has content after it
                .hasCount("comment", 2)  // Both ## lines are regular comments
                .token("scalar key", 1).hasText("firstFlow").and()
                .token("scalar key", 3).hasText("secondFlow");
    }
}
