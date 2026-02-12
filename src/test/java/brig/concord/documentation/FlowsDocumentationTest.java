package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class FlowsDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteFlows() {
        configureFromResource("/documentation/flows/concord.yaml");

        assertDocTarget(key("/flows"), "doc.flows.description",
                "/documentation/flows.html");

//        assertDocTarget(key("/flows/main"), "doc.flows.flowName.description",
//                "/documentation/flows.flowName.html");

        assertDocTarget(key("/flows/main[0]/suspend"), "doc.step.suspend.description",
                "/documentation/flows.flowName.suspend.html");

        assertDocTarget(key("/flows/main[1]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.name.html");

        // task
        assertDocTarget(key("/flows/main[14]/task"), "doc.step.task.description",
                "/documentation/flows.flowName.task.html");
        // call
        assertDocTarget(key("/flows/main[8]/call"), "doc.step.call.description",
                "/documentation/flows.flowName.call.html");
        // log
        assertDocTarget(key("/flows/main[15]/log"), "doc.step.log.description",
                "/documentation/flows.flowName.log.html");
        // logYaml
        assertDocTarget(key("/flows/main[16]/logYaml"), "doc.step.logYaml.description",
                "/documentation/flows.flowName.logYaml.html");
        // if
        assertDocTarget(key("/flows/main[17]/if"), "doc.step.if.description",
                "/documentation/flows.flowName.if.html");
        // checkpoint
        assertDocTarget(key("/flows/main[4]/checkpoint"), "doc.step.checkpoint.description",
                "/documentation/flows.flowName.checkpoint.html");
        // set
        assertDocTarget(key("/flows/main[12]/set"), "doc.step.set.description",
                "/documentation/flows.flowName.set.html");
        // throw
        assertDocTarget(key("/flows/main[7]/throw"), "doc.step.throw.description",
                "/documentation/flows.flowName.throw.html");
        // expr
        assertDocTarget(key("/flows/main[5]/expr"), "doc.step.expr.description",
                "/documentation/flows.flowName.expr.html");
        // parallel
        assertDocTarget(key("/flows/main[9]/parallel"), "doc.step.parallel.description",
                "/documentation/flows.flowName.parallel.html");
        // script
        assertDocTarget(key("/flows/main[11]/script"), "doc.step.script.description",
                "/documentation/flows.flowName.script.html");
        // switch
        assertDocTarget(key("/flows/main[13]/switch"), "doc.step.switch.description",
                "/documentation/flows.flowName.switch.html");
        // try
        assertDocTarget(key("/flows/main[10]/try"), "doc.step.try.description",
                "/documentation/flows.flowName.try.html");
        // block
        assertDocTarget(key("/flows/main[18]/block"), "doc.step.block.description",
                "/documentation/flows.flowName.block.html");
        // form
        assertDocTarget(key("/flows/main[6]/form"), "doc.step.form.description",
                "/documentation/flows.flowName.form.html");

        // task step features
        assertDocTarget(key("/flows/main[14]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.task.name.html");
        assertDocTarget(key("/flows/main[14]/in"), "doc.step.feature.in.description",
                "/documentation/flows.flowName.task.in.html");
        assertDocTarget(key("/flows/main[14]/out"), "doc.step.feature.out.description",
                "/documentation/flows.flowName.task.out.html");
        assertDocTarget(key("/flows/main[14]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.task.meta.html");
        assertDocTarget(key("/flows/main[14]/error"), "doc.step.feature.error.description",
                "/documentation/flows.flowName.task.error.html");
        assertDocTarget(key("/flows/main[14]/loop"), "doc.step.feature.loop.description",
                "/documentation/flows.flowName.task.loop.html");
        assertDocTarget(key("/flows/main[14]/retry"), "doc.step.feature.retry.description",
                "/documentation/flows.flowName.task.retry.html");
        assertDocTarget(key("/flows/main[14]/ignoreErrors"), "doc.step.feature.ignoreErrors.description",
                "/documentation/flows.flowName.task.ignoreErrors.html");

        // call step features
        assertDocTarget(key("/flows/main[8]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.call.name.html");
        assertDocTarget(key("/flows/main[8]/in"), "doc.step.feature.in.description",
                "/documentation/flows.flowName.call.in.html");
        assertDocTarget(key("/flows/main[8]/out"), "doc.step.feature.out.description",
                "/documentation/flows.flowName.call.out.html");
        assertDocTarget(key("/flows/main[8]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.call.meta.html");
        assertDocTarget(key("/flows/main[8]/error"), "doc.step.feature.error.description",
                "/documentation/flows.flowName.call.error.html");
        assertDocTarget(key("/flows/main[8]/loop"), "doc.step.feature.loop.description",
                "/documentation/flows.flowName.call.loop.html");
        assertDocTarget(key("/flows/main[8]/retry"), "doc.step.feature.retry.description",
                "/documentation/flows.flowName.call.retry.html");
    }
}
