package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class FlowCallInParamDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testInParamsAll() {
        configureFromResource("/documentation/flowcall-inparam/concord.yaml");

        assertDocTargetRaw(key("/flows/caller[0]/in"),
                "S3 bucket name (required)",
                "/documentation/flowcall-inparam.all.html");
    }

    @Test
    public void testInParamWithDescription() {
        configureFromResource("/documentation/flowcall-inparam/concord.yaml");

        assertDocTargetRaw(key("/flows/caller[0]/in/prefix"),
                "File prefix filter",
                "/documentation/flowcall-inparam.prefix.html");
    }

    @Test
    public void testInParamMandatory() {
        configureFromResource("/documentation/flowcall-inparam/concord.yaml");

        assertDocTargetRaw(key("/flows/caller[0]/in/s3Bucket"),
                "S3 bucket name",
                "/documentation/flowcall-inparam.s3Bucket.html");
    }

    @Test
    public void testInParamMandatoryNoDescription() {
        configureFromResource("/documentation/flowcall-inparam/concord.yaml");

        assertNoDocTarget(key("/flows/caller[0]/in/count"));
    }

    @Test
    public void testInParamNoFlowDoc() {
        configureFromResource("/documentation/flowcall-inparam/concord.yaml");

        assertNoDocTarget(key("/flows/noDocCaller[0]/in/x"));
    }
}
