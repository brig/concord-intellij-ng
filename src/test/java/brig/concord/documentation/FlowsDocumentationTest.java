// SPDX-License-Identifier: Apache-2.0
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class FlowsDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteFlows() {
        configureFromResource("/documentation/flows/concord.yaml");

        assertDocTarget(key("/flows"), "doc.flows.description",
                "/documentation/flows.html");

        assertDocTarget(key("/flows/main"), "doc.flows.flowName.description",
                "/documentation/flows.flowName.html");

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

        // form step features
        assertDocTarget(key("/flows/main[6]/yield"), "doc.step.feature.yield.description",
                "/documentation/flows.flowName.form.yield.html");
        assertDocTarget(key("/flows/main[6]/saveSubmittedBy"), "doc.step.feature.saveSubmittedBy.description",
                "/documentation/flows.flowName.form.saveSubmittedBy.html");
        assertDocTarget(key("/flows/main[6]/runAs"), "doc.step.feature.runAs.description",
                "/documentation/flows.flowName.form.runAs.html");
        assertDocTarget(key("/flows/main[6]/values"), "doc.step.feature.values.description",
                "/documentation/flows.flowName.form.values.html");
        assertDocTarget(key("/flows/main[6]/fields"), "doc.step.feature.fields.description",
                "/documentation/flows.flowName.form.fields.html");

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

        // log step features
        assertDocTarget(key("/flows/main[15]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.log.name.html");
        assertDocTarget(key("/flows/main[15]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.log.meta.html");

        // logYaml step features
        assertDocTarget(key("/flows/main[16]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.logYaml.name.html");
        assertDocTarget(key("/flows/main[16]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.logYaml.meta.html");

        // expr step features
        assertDocTarget(key("/flows/main[5]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.expr.name.html");
        assertDocTarget(key("/flows/main[5]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.expr.meta.html");
        assertDocTarget(key("/flows/main[5]/error"), "doc.step.feature.error.description",
                "/documentation/flows.flowName.expr.error.html");
        assertDocTarget(key("/flows/main[5]/out"), "doc.step.feature.out.description",
                "/documentation/flows.flowName.expr.out.html");

        // throw step features
        assertDocTarget(key("/flows/main[7]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.throw.name.html");

        // checkpoint step features
        assertDocTarget(key("/flows/main[4]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.checkpoint.meta.html");

        // parallel step features
        assertDocTarget(key("/flows/main[9]/out"), "doc.step.feature.out.description",
                "/documentation/flows.flowName.parallel.out.html");
        assertDocTarget(key("/flows/main[9]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.parallel.meta.html");

        // script step features
        assertDocTarget(key("/flows/main[11]/name"), "doc.step.feature.name.description",
                "/documentation/flows.flowName.script.name.html");
        assertDocTarget(key("/flows/main[11]/body"), "doc.step.feature.body.description",
                "/documentation/flows.flowName.script.body.html");
        assertDocTarget(key("/flows/main[11]/in"), "doc.step.feature.in.description",
                "/documentation/flows.flowName.script.in.html");
        assertDocTarget(key("/flows/main[11]/out"), "doc.step.feature.out.description",
                "/documentation/flows.flowName.script.out.html");
        assertDocTarget(key("/flows/main[11]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.script.meta.html");
        assertDocTarget(key("/flows/main[11]/error"), "doc.step.feature.error.description",
                "/documentation/flows.flowName.script.error.html");
        assertDocTarget(key("/flows/main[11]/loop"), "doc.step.feature.loop.description",
                "/documentation/flows.flowName.script.loop.html");
        assertDocTarget(key("/flows/main[11]/retry"), "doc.step.feature.retry.description",
                "/documentation/flows.flowName.script.retry.html");

        // if step features
        assertDocTarget(key("/flows/main[17]/then"), "doc.step.feature.then.description",
                "/documentation/flows.flowName.if.then.html");
        assertDocTarget(key("/flows/main[17]/else"), "doc.step.feature.else.description",
                "/documentation/flows.flowName.if.else.html");
        assertDocTarget(key("/flows/main[17]/meta"), "doc.step.feature.meta.description",
                "/documentation/flows.flowName.if.meta.html");
    }
}
