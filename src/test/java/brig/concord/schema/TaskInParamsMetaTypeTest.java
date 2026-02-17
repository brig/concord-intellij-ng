package brig.concord.schema;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.meta.ConcordMetaTypeProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskInParamsMetaTypeTest extends ConcordYamlTestBaseJunit5 {

    // --- findTaskName tests ---
    // findTaskName must only be called on elements whose resolved meta type
    // is TaskInParamsMetaType (i.e. values of in:/out: keys in a task step).

    @Test
    void testFindTaskName_fromInValue() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: start
                """);

        var inValue = value("/flows/main[0]/in").element();
        assertIsTaskInParams(inValue);
        assertEquals("concord", TaskInParamsMetaType.findTaskName(inValue));
    }

    @Test
    void testFindTaskName_differentTaskNames() {
        configureFromText("""
                flows:
                  main:
                    - task: s3
                      in:
                        action: getObject
                    - task: slack
                      in:
                        channelId: test
                """);

        var in1 = value("/flows/main[0]/in").element();
        assertIsTaskInParams(in1);
        assertEquals("s3", TaskInParamsMetaType.findTaskName(in1));

        var in2 = value("/flows/main[1]/in").element();
        assertIsTaskInParams(in2);
        assertEquals("slack", TaskInParamsMetaType.findTaskName(in2));
    }

    @Test
    void testFindTaskName_taskWithExpression() {
        configureFromText("""
                flows:
                  main:
                    - task: ${taskName}
                      in:
                        action: start
                """);

        var inValue = value("/flows/main[0]/in").element();
        assertIsTaskInParams(inValue);
        assertEquals("${taskName}", TaskInParamsMetaType.findTaskName(inValue));
    }

    @Test
    void testFindTaskName_inHasTaskParam() {
        // in: mapping contains a "task" key as a parameter — should still
        // resolve to the step-level task name, not the parameter value
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        task: myProject
                        action: start
                """);

        var inValue = value("/flows/main[0]/in").element();
        assertIsTaskInParams(inValue);
        assertEquals("concord", TaskInParamsMetaType.findTaskName(inValue));
    }

    @Test
    void testFindTaskName_fromOutValue() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: start
                      out:
                        result: ok
                """);

        var outValue = value("/flows/main[0]/out").element();
        assertIsTaskOutParams(outValue);
        assertEquals("concord", TaskInParamsMetaType.findTaskName(outValue));
    }

    @Test
    void testMetaType_callStepInIsNotTaskInParams() {
        configureFromText("""
                flows:
                  main:
                    - call: someFlow
                      in:
                        param1: value1
                """);

        var inValue = value("/flows/main[0]/in").element();
        var metaType = ConcordMetaTypeProvider.getInstance(getProject()).getResolvedMetaType(inValue);
        assertNotInstanceOf(TaskInParamsMetaType.class, metaType);
    }


    @Test
    void testMetaType_taskStepInParams() {
        configureFromText("""
                flows:
                  main:
                    - task: someTask
                      in:
                        param1: value1
                """);

        var element = key("/flows/main[0]/in/param1").element();
        assertIsTaskInParams(element);
    }

    @Test
    void testMetaType_taskStepInParams2() {
        configureFromText("""
                flows:
                  main:
                    - task: someTask
                      in:
                        param1:
                          param11: value1
                """);

        var element = key("/flows/main[0]/in/param1/param11").element();
        var metaType = ConcordMetaTypeProvider.getInstance(getProject()).getResolvedMetaType(element);
        assertNotInstanceOf(TaskInParamsMetaType.class, metaType);
    }

    private void assertIsTaskInParams(com.intellij.psi.PsiElement element) {
        var metaType = ConcordMetaTypeProvider.getInstance(getProject()).getResolvedMetaType(element);
        assertInstanceOf(TaskInParamsMetaType.class, metaType);
    }

    private void assertIsTaskOutParams(com.intellij.psi.PsiElement element) {
        var metaType = ConcordMetaTypeProvider.getInstance(getProject()).getResolvedMetaType(element);
        assertInstanceOf(TaskOutParamsMetaType.class, metaType);
    }

    private static void assertNotInstanceOf(Class<?> unexpected, Object actual) {
        if (unexpected.isInstance(actual)) {
            fail("Expected NOT an instance of " + unexpected.getSimpleName() + ", but got: " + actual.getClass().getSimpleName());
        }
    }
}
