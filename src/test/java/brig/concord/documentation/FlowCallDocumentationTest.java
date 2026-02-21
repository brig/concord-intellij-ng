// SPDX-License-Identifier: Apache-2.0
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class FlowCallDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testFlowCallWithDocumentation() {
        configureFromResource("/documentation/flowcall/concord.yaml");

        assertDocTargetRaw(value("/flows/main[0]/call"),
                "Process S3 files and return count",
                "/documentation/flowcall/processS3.html");
    }

    @Test
    public void testFlowCallQuoted() {
        configureFromResource("/documentation/flowcall/concord.yaml");

        assertDocTargetRaw(value("/flows/main[1]/call"),
                "Quoted call target flow",
                "/documentation/flowcall/quotedCall.html");
    }

    @Test
    public void testFlowCallWithoutDocumentation() {
        configureFromResource("/documentation/flowcall/concord.yaml");

        assertNoDocTarget(value("/flows/main[2]/call"));
    }
}
