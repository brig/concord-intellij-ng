package brig.concord.lexer;

import org.junit.jupiter.api.Test;

import static brig.concord.assertions.TokenAssertions.assertTokens;

class FlowDocLexerTest {

    @Test
    void testFlowDocTokens() {
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
    void testMultipleFlowDocs() {
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
    void testCustomTagsInFlowDoc() {
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
    void testFlowDocWithEmptyLines() {
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
    void testCommentPrefixIsSeparateToken() {
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
    void testHashWithoutSpaceInDescription() {
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
    void testHashWithoutSpaceInParams() {
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
    void testMandatoryAndOptionalTokens() {
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
    void testRequiredAsAliasForMandatory() {
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
    void testEmptyCommentLine() {
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
    void testColonAndCommaTokens() {
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
    void testArbitraryIndentation() {
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
    void testUserTagWithSameIndentAsSection() {
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
    void testUserTagWithSmallerIndentThanSection() {
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
    void testNestedSectionIsNotRecognized() {
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
    void testSameIndentSectionsRecognized() {
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
    void testCommentsUnderConfiguration() {
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
    void testCommentsUnderFlowName() {
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
    void testTripleHashInsideFlowDoc() {
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
    void testFlowDocWithYamlLikeContent() {
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
    void testFlowDocMarkerWithTabs() {
        // ## followed by tabs should still close flow doc
        var yaml = "flows:\n  ##\n  # desc\n  ##\t\n  myFlow:\n    - log: test\n";

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }

    @Test
    void testFlowDocMarkerFollowedBySpacesAndContent() {
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
    void testEmptyFlowDoc() {
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
    void testFlowDocAtEndOfFile() {
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
    void testFlowDocAtEndOfFile2() {
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
    void testFlowDocOutsideFlowsSection() {
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
    void testFlowDocWithColonInDescription() {
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
    void testConsecutiveFlowDocs() {
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
    void testFlowDocWithOnlyMarkers() {
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
    void testHashHashHashHash() {
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
    void testFlowDocInsideQuotedString() {
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
    void testFlowDocWithDashDashContent() {
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
    void testTwoFlowsWithHashHashComments() {
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

    @Test
    void testFlowDocParamTypeWithSpaces() {
        var yaml = """
                flows:
                  ##
                  # in:
                  #   myParam: invalid param with space, required
                  ##
                  myFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .tokenHasText("FLOW_DOC_TYPE", "invalid param with space");
    }

    @Test
    void testFlowDocMarkerInsideFlowSteps() {
        var yaml = """
                flows:
                  myFlow:
                      ##
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0);
    }

    @Test
    void testFlowDocMarkerOnlyAtFlowDefLevel() {
        // ## at flow definition level should be FLOW_DOC_MARKER
        // ## inside flow steps should be regular comment
        var yaml = """
                flows:
                  ##
                  # First flow doc
                  ##
                  firstFlow:
                    ##
                    - log: "inside flow - comment"
                  ##
                  # Second flow doc
                  ##
                  secondFlow:
                    - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 4)
                .hasCount("comment", 1);
    }

    @Test
    void testFlowDocMarkerDeeplyNested() {
        // ## deeply nested should always be comment, not flow doc marker
        var yaml = """
                flows:
                  myFlow:
                    - if: true
                      then:
                        ##
                        - log: "nested"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0)
                .hasCount("comment", 1);
    }

    @Test
    void testFlowDocFlowsQuoted() {
        var yaml = """
                "flows":
                    ##
                    # Flow description
                    ##
                    myFlow:
                        - log: "nested"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .tokenHasText("FLOW_DOC_CONTENT", "Flow description");
    }

    @Test
    void testFlowDocFlowsSpace() {
        var yaml = """
                flows :
                    ##
                    # Flow description
                    ##
                    myFlow:
                        - log: "nested"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .tokenHasText("FLOW_DOC_CONTENT", "Flow description");
    }

    @Test
    void testFlowDocFlowsInitialIndent() {
        var yaml = "    flows:\n" +
                   "        ##\n" +
                   "        # Flow description\n" +
                   "        ##\n" +
                   "        myFlow:\n" +
                   "            - log: \"nested\"\n";

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .tokenHasText("FLOW_DOC_CONTENT", "Flow description");
    }

    @Test
    void testFlowDocFlowsSingleQuoted() {
        var yaml = """
                'flows':
                    ##
                    # Flow description
                    ##
                    myFlow:
                        - log: "test"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .tokenHasText("FLOW_DOC_CONTENT", "Flow description");
    }

    @Test
    void testFlowDocExitOnNextTopLevelKey() {
        // ## after configuration: should NOT be flow doc marker
        var yaml = """
                flows:
                  ##
                  # Flow doc
                  ##
                  myFlow:
                    - log: "test"
                configuration:
                  ##
                  runtime: concord-v2
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2)
                .hasCount("comment", 1);
    }

    @Test
    void testFlowDocInProfiles() {
        // flows inside profiles should NOT support flow doc
        var yaml = """
                profiles:
                  myProfile:
                    flows:
                      ##
                      # Profile flow doc
                      ##
                      profileFlow:
                        - log: "in profile"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0);
    }

    @Test
    void testFlowDocMultipleFlowsSections() {
        // Only top-level `flows` should work
        var yaml = """
                flows:
                  ##
                  # Top level flow
                  ##
                  topFlow:
                    - log: "top"
                profiles:
                  dev:
                    flows:
                      ##
                      # Profile flow
                      ##
                      devFlow:
                        - log: "dev"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }

    @Test
    void testNonFlowDocInConfigurationSection() {
        var yaml = """
                flows:
                  myFlow:
                    - log: "test"
                configuration:
                  runtime: concord-v2
                  flows:
                    ##
                    # Not a flow DOC
                    ##
                    myFlow:
                      - log: "BOOM"
                """;

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 0);
    }

    @Test
    void testFlowDocTokensWithAdditionalCharsInHeader() {
        var yaml = """
                flows:
                  ## ----
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
                .hasCount("FLOW_DOC_MARKER", 1);
    }
}
