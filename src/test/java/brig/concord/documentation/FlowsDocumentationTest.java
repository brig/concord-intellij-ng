package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class FlowsDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteFlows() {
        configureFromResource("/documentation/flows/concord.yaml");

//        assertDocTarget(key("/flows"), "doc.flows.description",
//                "/documentation/flows.html");
//
//        assertDocTarget(key("/flows/main"), "doc.flows.flowName.description",
//                "/documentation/flows.flowName.html");

        assertDocTarget(key("/flows/main[0]/suspend"), "doc.flows.flowName.suspend.description",
                "/documentation/flows.flowName.suspend.html");

        assertNoDocTarget(value("/publicFlows[0]"));
    }
}
