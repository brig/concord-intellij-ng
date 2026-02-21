// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class PublicFlowsDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompletePublicFlows() {
        configureFromText("""
                flows:
                  main:
                    - log: "Hello"

                publicFlows:
                  - main""");

        assertDocTarget(key("/publicFlows"), "doc.publicFlows.description",
                "/documentation/publicFlows.html");

        assertNoDocTarget(value("/publicFlows[0]"));
    }
}
