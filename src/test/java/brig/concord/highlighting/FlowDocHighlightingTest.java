package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class FlowDocHighlightingTest extends HighlightingTestBase {

    @Test
    void testFlowDocMarkerHighlighting() {
        configureFromText("""
            flows:
              ##
              # Process S3 files
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              ##
              processS3:
                - task: s3
            """);

        // ## at the start of flow doc should be highlighted as FLOW_DOC_MARKER
        highlight(flowDoc("/flows/processS3")).is(ConcordHighlightingColors.FLOW_DOC_MARKER);
    }

    @Test
    void testFlowDocParamHighlighting() {
        configureFromText("""
            flows:
              ##
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              ##
              processS3:
                - task: s3
            """);

        highlight(flowDocParam("/flows/processS3", "s3Bucket")).is(ConcordHighlightingColors.FLOW_DOC_PARAM_NAME);
    }

    @Test
    void testFlowDocHighlightingAfterEdit() {
        // Regression: editing inside a flow doc block causes the entire block
        // to lose syntax highlighting (turns gray) because incremental re-lexing
        // loses the flow doc context.
        configureFromText("""
            flows:
              ##
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name<caret>
              ##
              processS3:
                - task: s3
            """);

        // Verify highlighting is correct before edit
        highlight(flowDoc("/flows/processS3")).is(ConcordHighlightingColors.FLOW_DOC_MARKER);
        highlight(flowDocParam("/flows/processS3", "s3Bucket")).is(ConcordHighlightingColors.FLOW_DOC_PARAM_NAME);

        // Edit inside the flow doc - append text to the parameter description
        myFixture.type(" extra");

        // After edit, flow doc tokens must still be highlighted correctly
        highlight(flowDoc("/flows/processS3")).is(ConcordHighlightingColors.FLOW_DOC_MARKER);
        highlight(flowDocParam("/flows/processS3", "s3Bucket")).is(ConcordHighlightingColors.FLOW_DOC_PARAM_NAME);
    }

    @Test
    void testFlowDocHighlightingAfterDelete() {
        configureFromText("""
            flows:
              ##
              # in:
              #   bucket: string, mandatory, Bucket<caret> name
              ##
              myFlow:
                - task: s3
            """);

        highlight(flowDoc("/flows/myFlow")).is(ConcordHighlightingColors.FLOW_DOC_MARKER);
        highlight(flowDocParam("/flows/myFlow", "bucket")).is(ConcordHighlightingColors.FLOW_DOC_PARAM_NAME);

        // Backspace
        myFixture.type("\b");

        highlight(flowDoc("/flows/myFlow")).is(ConcordHighlightingColors.FLOW_DOC_MARKER);
        highlight(flowDocParam("/flows/myFlow", "bucket")).is(ConcordHighlightingColors.FLOW_DOC_PARAM_NAME);
    }
}