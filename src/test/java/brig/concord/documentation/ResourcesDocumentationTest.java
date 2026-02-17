// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class ResourcesDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteResource() {
        configureFromText("""
                flows:
                  main:
                    - log: "Hello"

                resources:
                  concord:
                      - "glob:concord/{**/,}{*.,}concord.yml""");

        assertDocTarget(key("/resources"), "doc.resources.description",
                "/documentation/resources.html");

        assertDocTarget(key("/resources/concord"), "doc.resources.concord.description",
                "/documentation/resources.concord.html");
    }
}
