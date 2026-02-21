// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CallOutCompletionTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testOutScalarCompletion() {
        configureFromText(
                """
                        flows:
                          default:
                            - call: "processS3"
                              out: <caret>
                          ##
                          # Process S3 files
                          # in:
                          #   s3Bucket: string, mandatory, S3 bucket name
                          # out:
                          #   processed: int, Files processed count
                          #   status: string, Processing status
                          ##
                          processS3:
                            - log: "hello"
                        """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder("processed", "status");
    }

    @Test
    void testOutArrayCompletion() {
        configureFromText(
                """
                        flows:
                          default:
                            - call: "processS3"
                              out:
                                - <caret>
                          ##
                          # out:
                          #   processed: int, Files processed count
                          #   status: string, Processing status
                          ##
                          processS3:
                            - log: "hello"
                        """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings, "lookupElementStrings is null");
        assertThat(lookupElementStrings).containsExactlyInAnyOrder("processed", "status");
    }

    @Test
    void testOutArrayCompletionCrossFile() {
        createFile("project-a/concord/utils.concord.yaml",
                """
                        flows:
                          ##
                          # out:
                          #   processed: int, Files processed count
                          #   status: string, Processing status
                          ##
                          processS3:
                            - log: "hello"
                        """);

        var main = createFile("project-a/concord.yaml",
                """
                        flows:
                          default:
                            - call: "processS3"
                              out:
                                - <caret>
                        """);

        configureFromExistingFile(main);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings, "lookupElementStrings is null");
        assertThat(lookupElementStrings).containsExactlyInAnyOrder("processed", "status");
    }

    @Test
    void testOutNoFlowDoc() {
        configureFromText(
                """
                        flows:
                          default:
                            - call: "processS3"
                              out: <caret>
                          processS3:
                            - log: "hello"
                        """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        if (lookupElementStrings != null) {
            assertThat(lookupElementStrings).isEmpty();
        }
    }

    @Test
    void testOutNoOutSection() {
        configureFromText(
                """
                        flows:
                          default:
                            - call: "processS3"
                              out: <caret>
                          ##
                          # in:
                          #   s3Bucket: string, mandatory, S3 bucket name
                          ##
                          processS3:
                            - log: "hello"
                        """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        if (lookupElementStrings != null) {
            assertThat(lookupElementStrings).isEmpty();
        }
    }
}
