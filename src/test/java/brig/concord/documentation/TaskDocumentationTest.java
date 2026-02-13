package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class TaskDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testConcordTaskDocumentation() {
        configureFromResource("/documentation/task/concord.yaml");

        assertDocTargetRaw(value("/flows/main[0]/task"),
                "Schema for the concord task - start/fork processes, manage API keys",
                "/documentation/task/concord-task.html");
    }

    @Test
    public void testStrictTaskDocumentation() {
        configureFromResource("/documentation/task/concord.yaml");

        assertDocTargetRaw(value("/flows/main[1]/task"),
                "An HTTP client task for making web requests",
                "/documentation/task/strictTask.html");
    }

    @Test
    public void testConditionalParamsBelowThreshold() {
        configureFromResource("/documentation/task/concord.yaml");

        assertDocTargetRaw(value("/flows/main[3]/task"),
                "A task with multiple discriminator keys",
                "/documentation/task/multiKeyTask.html");
    }

    @Test
    public void testUnknownTaskNoDocumentation() {
        configureFromResource("/documentation/task/concord.yaml");

        assertNoDocTarget(value("/flows/main[2]/task"));
    }
}
