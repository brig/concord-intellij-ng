// SPDX-License-Identifier: Apache-2.0
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class TaskInParamDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testTaskInParamWithDescription() {
        configureFromResource("/documentation/task/concord.yaml");

        assertDocTargetRaw(key("/flows/main[1]/in/url"),
                "URL to fetch",
                "/documentation/task/inparam.url.html");
    }

    @Test
    public void testTaskInParamEnum() {
        configureFromResource("/documentation/task/concord.yaml");

        assertDocTargetRaw(key("/flows/main[1]/in/method"),
                "HTTP method",
                null);
    }
}
