package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.meta.model.AnyOfType;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static brig.concord.completion.provider.FlowCallParamsProvider.*;

public class ErrorInspectionTests extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(MissingKeysInspection.class, UnknownKeysInspection.class, ValueInspection.class);
    }

    @Test
    public void testCheckpoint_000() {
        configureFromResource("/errors/checkpoint/000.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testCheckpoint_001() {
        configureFromResource("/errors/checkpoint/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testCheckpoint_002() {
        configureFromResource("/errors/checkpoint/002.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testCheckpoint_003() {
        configureFromResource("/errors/checkpoint/003.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testCheckpoint_004() {
        configureFromResource("/errors/checkpoint/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testCheckpoint_005() {
        configureFromResource("/errors/checkpoint/005.concord.yml");
        inspection()
                .assertUnexpectedKey("trash")
                .check();
    }

    @Test
    public void testExpression_000() {
        configureFromResource("/errors/expression/000.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_001() {
        configureFromResource("/errors/expression/001.concord.yml");
        inspection()
                .assertHasError(ConcordBundle.message("ExpressionType.error.invalid.value"))
                .check();
    }

    @Test
    public void testExpression_002() {
        configureFromResource("/errors/expression/002.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_003() {
        configureFromResource("/errors/expression/003.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testExpression_005() {
        configureFromResource("/errors/expression/005.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_006() {
        configureFromResource("/errors/expression/006.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testExpression_007() {
        configureFromResource("/errors/expression/007.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_008() {
        configureFromResource("/errors/expression/008.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    // TODO: IdentityElementMetaType.validateValue
    @Test
    public void testExpression_009() {
        configureFromResource("/errors/expression/009.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testExpression_010() {
        configureFromResource("/errors/expression/010.concord.yml");
        inspection()
                .assertUnknownStep()
                .check();
    }

    @Test
    public void testImports_001() {
        configureFromResource("/errors/imports/001.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testImports_002() {
        configureFromResource("/errors/imports/002.concord.yml");
        inspection()
                .assertArrayRequired()
                .assertUnexpectedKey("k")
                .check();
    }

    @Test
    public void testImports_002_1() {
        configureFromResource("/errors/imports/002_1.concord.yml");
        inspection()
                .assertUnexpectedKey("k")
                .check();
    }

    @Test
    public void testImports_003() {
        configureFromResource("/errors/imports/003.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testImports_004() {
        configureFromResource("/errors/imports/004.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testImports_005() {
        configureFromResource("/errors/imports/005.concord.yml");
        inspection()
                .assertValueRequired()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testImports_006() {
        configureFromResource("/errors/imports/006.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_007() {
        configureFromResource("/errors/imports/007.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_008() {
        configureFromResource("/errors/imports/008.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testImports_009() {
        configureFromResource("/errors/imports/009.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_010() {
        configureFromResource("/errors/imports/010.concord.yml");
        inspection()
                .assertMissingKey("name")
                .check();
    }

    @Test
    public void testImports_011() {
        configureFromResource("/errors/imports/011.concord.yml");
        inspection()
                .assertUnexpectedKey("git-trash")
                .check();
    }

    @Test
    public void testImports_012() {
        configureFromResource("/errors/imports/012.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testImports_013() {
        configureFromResource("/errors/imports/013.concord.yml");
        inspection()
                .assertUnexpectedKey("trash")
                .check();
    }

    @Test
    public void testImports_014() {
        configureFromResource("/errors/imports/014.concord.yml");
        inspection()
                .assertObjectRequired()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testImports_015() {
        configureFromResource("/errors/imports/015.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_016() {
        configureFromResource("/errors/imports/016.concord.yml");
        inspection()
                .assertHasError("Valid regular expression or string required. Error: 'Unclosed character class near index 1\n" +
                        "[.\n" +
                        " ^'")
                .check();
    }

    @Test
    public void testTriggers_001() {
        configureFromResource("/errors/triggers/001.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_002() {
        configureFromResource("/errors/triggers/002.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_003() {
        configureFromResource("/errors/triggers/003.concord.yml");
        inspection()
                .assertMissingKey("conditions, entryPoint")
                .assertIntExpected()
                .check();
    }

    @Test
    public void testTriggers_004() {
        configureFromResource("/errors/triggers/004.concord.yml");
        inspection()
                .assertMissingKey("conditions, entryPoint")
                .check();
    }

    @Test
    public void testTriggers_005() {
        configureFromResource("/errors/triggers/005.concord.yml");
        inspection()
                .assertStringValueExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testTriggers_006() {
        configureFromResource("/errors/triggers/006.concord.yml");
        inspection()
                .assertMissingKey("conditions")
                .check();
    }

    @Test
    public void testTriggers_007() {
        configureFromResource("/errors/triggers/007.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_008() {
        configureFromResource("/errors/triggers/008.concord.yml");

        inspection()
                .assertMissingKey("type")
                .assertUnexpectedKey("test")
                .check();
    }

    @Test
    public void testTriggers_009() {
        configureFromResource("/errors/triggers/009.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_010() {
        configureFromResource("/errors/triggers/010.concord.yml");
        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testTriggers_011() {
        configureFromResource("/errors/triggers/011.concord.yml");
        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testTriggers_012() {
        configureFromResource("/errors/triggers/012.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_013() {
        configureFromResource("/errors/triggers/013.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_014() {
        configureFromResource("/errors/triggers/014.concord.yml");
        inspection()
                .assertHasError("Valid regular expression or string required. Error: 'Dangling meta character '*' near index 0\n" +
                        "*\n" +
                        "^'")
                .check();
    }

    @Test
    public void testTriggers_015() {
        configureFromResource("/errors/triggers/015.concord.yml");
        inspection()
                .assertMissingKey("conditions, entryPoint")
                .check();
    }

    @Test
    public void testTriggers_016() {
        configureFromResource("/errors/triggers/016.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_017() {
        configureFromResource("/errors/triggers/017.concord.yml");
        inspection()
                .assertMissingKey("entryPoint")
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTriggers_018() {
        configureFromResource("/errors/triggers/018.concord.yml");
        inspection()
                .assertStringValueExpected()
                .assertStringValueExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testTriggers_019() {
        configureFromResource("/errors/triggers/019.concord.yml");
        inspection()
                .assertStringValueExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testTriggers_020() {
        configureFromResource("/errors/triggers/020.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_021() {
        configureFromResource("/errors/triggers/021.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_022() {
        configureFromResource("/errors/triggers/022.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_023() {
        configureFromResource("/errors/triggers/023.concord.yml");
        inspection()
                .assertMissingKey("entryPoint")
                .check();
    }

    @Test
    public void testTriggers_024() {
        configureFromResource("/errors/triggers/024.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("oneops")
    public void testTriggers_025() {
        configureFromResource("/errors/triggers/025.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("oneops")
    public void testTriggers_026() {
        configureFromResource("/errors/triggers/026.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("oneops")
    public void testTriggers_027() {
        configureFromResource("/errors/triggers/027.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("custom trigger")
    public void testTriggers_028() {
        configureFromResource("/errors/triggers/028.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_029() {
        configureFromResource("/errors/triggers/029.concord.yml");
        inspection()
                .assertSingleValueExpected()
                .check();
    }

    @Test
    public void testTriggers_030() {
        configureFromResource("/errors/triggers/030.concord.yml");
        inspection()
                .assertHasError("Valid timezone required")
                .check();
    }

    @Test
    public void testTriggers_031() {
        configureFromResource("/errors/triggers/031.concord.yml");
        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testTriggers_031_1() {
        configureFromResource("/errors/triggers/031_1.concord.yml");
        inspection()
                .assertUnknownKey("unknown")
                .check();
    }

    @Test
    public void testTriggers_032() {
        configureFromResource("/errors/triggers/032.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_033() {
        configureFromResource("/errors/triggers/033.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_034() {
        configureFromResource("/errors/triggers/034.concord.yml");
        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testTriggers_035() {
        configureFromResource("/errors/triggers/035.concord.yml");
        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testTriggers_036() {
        configureFromResource("/errors/triggers/036.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTriggers_037() {
        configureFromResource("/errors/triggers/037.concord.yml");
        inspection()
                .assertMissingKey("group or groupBy")
                .check();
    }

    @Test
    public void testTasks_000() {
        configureFromResource("/errors/tasks/000.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_001() {
        configureFromResource("/errors/tasks/001.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTasks_002() {
        configureFromResource("/errors/tasks/002.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_003() {
        configureFromResource("/errors/tasks/003.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTasks_005() {
        configureFromResource("/errors/tasks/005.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_006() {
        configureFromResource("/errors/tasks/006.concord.yml");
        inspection()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testTasks_007() {
        configureFromResource("/errors/tasks/007.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_008() {
        configureFromResource("/errors/tasks/008.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_009() {
        configureFromResource("/errors/tasks/009.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_009_1() {
        configureFromResource("/errors/tasks/009_1.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_009_2() {
        configureFromResource("/errors/tasks/009_2.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_009_3() {
        configureFromResource("/errors/tasks/009_3.concord.yml");
        inspection()
                .assertInvalidValue(NUMBER_OR_EXPRESSION)
                .check();
    }

    @Test
    public void testTasks_009_4() {
        configureFromResource("/errors/tasks/009_4.concord.yml");
        inspection()
                .assertUnexpectedValue("a")
                .check();
    }

    @Test
    public void testTasks_010() {
        configureFromResource("/errors/tasks/010.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_011() {
        configureFromResource("/errors/tasks/011.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_012() {
        configureFromResource("/errors/tasks/012.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_013() {
        configureFromResource("/errors/tasks/013.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_014() {
        configureFromResource("/errors/tasks/014.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_015() {
        configureFromResource("/errors/tasks/015.concord.yml");
        inspection()
                .assertUnexpectedKey("trash")
                .check();
    }

    @Test
    public void testTasks_016() {
        configureFromResource("/errors/tasks/016.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_017() {
        configureFromResource("/errors/tasks/017.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_018() {
        configureFromResource("/errors/tasks/018.concord.yml");

        inspection()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testTasks_019() {
        configureFromResource("/errors/tasks/019.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTasks_020() {
        configureFromResource("/errors/tasks/020.concord.yml");

        inspection()
                .assertUnexpectedValue("trash")
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testTasks_021() {
        configureFromResource("/errors/tasks/021.concord.yml");

        inspection()
                .assertInvalidValue(NUMBER_OR_EXPRESSION)
                .check();
    }

    @Test
    public void testFlowCall_000() {
        configureFromResource("/errors/flowCall/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_001() {
        configureFromResource("/errors/flowCall/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_002() {
        configureFromResource("/errors/flowCall/002.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_003() {
        configureFromResource("/errors/flowCall/003.concord.yml");

        inspection()
                .assertStringValueExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_005() {
        configureFromResource("/errors/flowCall/005.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_006() {
        configureFromResource("/errors/flowCall/006.concord.yml");

        inspection()
                .assertExpressionExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_007() {
        configureFromResource("/errors/flowCall/007.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_008() {
        configureFromResource("/errors/flowCall/008.concord.yml");

        inspection()
                .assertUnknownKey("withItems")
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_009() {
        configureFromResource("/errors/flowCall/009.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_010() {
        configureFromResource("/errors/flowCall/010.concord.yml");

        inspection()
                .assertObjectRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_011() {
        configureFromResource("/errors/flowCall/011.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_012() {
        configureFromResource("/errors/flowCall/012.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_013() {
        configureFromResource("/errors/flowCall/013.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_014() {
        configureFromResource("/errors/flowCall/014.concord.yml");

        inspection()
                .assertObjectRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_015() {
        configureFromResource("/errors/flowCall/015.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_016() {
        configureFromResource("/errors/flowCall/016.concord.yml");

        inspection()
                .assertValueRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_017() {
        configureFromResource("/errors/flowCall/017.concord.yml");

        inspection()
                .assertObjectRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_018() {
        configureFromResource("/errors/flowCall/018.concord.yml");

        inspection()
                .assertStringValueExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_019() {
        configureFromResource("/errors/flowCall/019.concord.yml");

        inspection()
                .assertObjectRequired()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_020() {
        configureFromResource("/errors/flowCall/020.concord.yml");

        inspection()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testFlowCall_021() {
        configureFromResource("/errors/flowCall/021.concord.yml");

        inspection()
                .assertUndefinedFlow()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testFlowCall_022() {
        configureFromResource("/errors/flowCall/022.concord.yml");

        inspection()
                .assertUnexpectedKey("unknown")
                .check();
    }

    @Test
    public void testFlowCall_023() {
        configureFromResource("/errors/flowCall/023.concord.yml");

        inspection()
                .assertInvalidValue(STRING_OR_EXPRESSION)
                .check();
    }

    @Test
    public void testGroup_000() {
        configureFromResource("/errors/group/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_001() {
        configureFromResource("/errors/group/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testGroup_002() {
        configureFromResource("/errors/group/002.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testGroup_003() {
        configureFromResource("/errors/group/003.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testGroup_004() {
        configureFromResource("/errors/group/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_005() {
        configureFromResource("/errors/group/005.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_006() {
        configureFromResource("/errors/group/006.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_007() {
        configureFromResource("/errors/group/007.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testGroup_008() {
        configureFromResource("/errors/group/008.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testParallel_000() {
        configureFromResource("/errors/parallel/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testParallel_001() {
        configureFromResource("/errors/parallel/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testParallel_002() {
        configureFromResource("/errors/parallel/002.concord.yml");

        inspection()
                .assertUnknownStep()
                .check();
    }

    @Test
    public void testParallel_003() {
        configureFromResource("/errors/parallel/003.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testParallel_004() {
        configureFromResource("/errors/parallel/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testParallel_005() {
        configureFromResource("/errors/parallel/005.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testForms_000() {
        configureFromResource("/errors/forms/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testForms_001() {
        configureFromResource("/errors/forms/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testForms_002() {
        configureFromResource("/errors/forms/002.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testForms_003() {
        configureFromResource("/errors/forms/003.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testForms_004() {
        configureFromResource("/errors/forms/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testForms_005() {
        configureFromResource("/errors/forms/005.concord.yml");

        inspection()
                .assertUnexpectedKey("error")
                .check();
    }

    @Test
    public void testForms_006() {
        configureFromResource("/errors/forms/006.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testConfiguration_000() {
        configureFromResource("/errors/configuration/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_001() {
        configureFromResource("/errors/configuration/001.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_002() {
        configureFromResource("/errors/configuration/002.concord.yml");

        inspection()
                .assertStringValueExpected()
                .assertUndefinedFlow()
                .check();
    }

    @Test
    public void testConfiguration_003() {
        configureFromResource("/errors/configuration/003.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }
    @Test
    public void testConfiguration_004() {
        configureFromResource("/errors/configuration/004.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testConfiguration_005() {
        configureFromResource("/errors/configuration/005.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_005_1() {
        configureFromResource("/errors/configuration/005_1.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_006() {
        configureFromResource("/errors/configuration/006.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testConfiguration_007() {
        configureFromResource("/errors/configuration/007.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_008() {
        configureFromResource("/errors/configuration/008.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_009() {
        configureFromResource("/errors/configuration/009.concord.yml");

        inspection()
                .assertDurationExpected()
                .check();
    }

    @Test
    public void testConfiguration_010() {
        configureFromResource("/errors/configuration/010.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_011() {
        configureFromResource("/errors/configuration/011.concord.yml");

        inspection()
                .assertMissingKey("group")
                .check();
    }

    @Test
    public void testConfiguration_011_1() {
        configureFromResource("/errors/configuration/011_1.concord.yml");

        inspection()
                .assertHasError(ConcordBundle.message("StringType.error.empty.scalar.value"))
                .check();
    }

    @Test
    public void testConfiguration_012() {
        configureFromResource("/errors/configuration/012.concord.yml");

        inspection()
                .assertUnknownKey("mode1")
                .check();
    }

    @Test
    public void testConfiguration_013() {
        configureFromResource("/errors/configuration/013.concord.yml");

        inspection()
                .assertUnexpectedValue("canceL")
                .check();
    }

    @Test
    public void testConfiguration_014() {
        configureFromResource("/errors/configuration/014.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_015() {
        configureFromResource("/errors/configuration/015.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_016() {
        configureFromResource("/errors/configuration/016.concord.yml");

        inspection()
                .assertUnexpectedValue("1")
                .check();
    }

    @Test
    public void testConfiguration_017() {
        configureFromResource("/errors/configuration/017.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_018() {
        configureFromResource("/errors/configuration/018.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testConfiguration_019() {
        configureFromResource("/errors/configuration/019.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_020() {
        configureFromResource("/errors/configuration/020.concord.yml");

        inspection()
                .assertSingleValueExpected()
                .assertSingleValueExpected()
                .check();
    }

    @Test
    public void testConfiguration_021() {
        configureFromResource("/errors/configuration/021.concord.yml");

        inspection()
                .assertUnexpectedValue("1")
                .check();
    }

    @Test
    public void testConfiguration_022() {
        configureFromResource("/errors/configuration/022.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_023() {
        configureFromResource("/errors/configuration/023.concord.yml");

        inspection()
                .assertDurationExpected()
                .check();
    }

    @Test
    public void testForms_007() {
        configureFromResource("/errors/forms/007.concord.yml");

        inspection()
                .assertUnexpectedValue("123")
                .check();
    }

    @Test
    public void testForms_008() {
        configureFromResource("/errors/forms/008.concord.yml");

        inspection()
                .assertMissingKey("type")
                .check();
    }

    @Test
    public void testForms_009() {
        configureFromResource("/errors/forms/009.concord.yml");

        inspection()
                .assertUnexpectedValue("123")
                .check();
    }

    @Test
    public void testForms_010() {
        configureFromResource("/errors/forms/010.concord.yml");

        inspection()
                .assertUnexpectedValue("1")
                .check();
    }

    @Test
    public void testForms_011() {
        configureFromResource("/errors/forms/011.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testForms_012() {
        configureFromResource("/errors/forms/012.concord.yml");

        inspection()
                .assertUnexpectedKey("min")
                .assertUnexpectedKey("max")
                .check();
    }

    @Test
    public void testFormCall_000() {
        configureFromResource("/errors/formCall/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFormCall_001() {
        configureFromResource("/errors/formCall/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testFormCall_002() {
        configureFromResource("/errors/formCall/002.concord.yml");

        inspection()
                .assertUnexpectedKey("a")
                .assertUnexpectedKey("b")
                .check();
    }

    @Test
    public void testFormCall_003() {
        configureFromResource("/errors/formCall/003.concord.yml");

        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testFormCall_004() {
        configureFromResource("/errors/formCall/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFormCall_005() {
        configureFromResource("/errors/formCall/005.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFormCall_006() {
        configureFromResource("/errors/formCall/006.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFormCall_007() {
        configureFromResource("/errors/formCall/007.concord.yml");

        inspection()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testFlows_000() {
        configureFromResource("/errors/flows/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlows_001() {
        configureFromResource("/errors/flows/001.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFlows_002() {
        configureFromResource("/errors/flows/002.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlows_003() {
        configureFromResource("/errors/flows/003.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testProfiles_000() {
        configureFromResource("/errors/profiles/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testProfiles_001() {
        configureFromResource("/errors/profiles/001.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testProfiles_002() {
        configureFromResource("/errors/profiles/002.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testProfiles_003() {
        configureFromResource("/errors/profiles/003.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testIf_000() {
        configureFromResource("/errors/if/000.concord.yml");

        inspection()
                .assertValueRequired()
                .assertMissingKey("then")
                .check();
    }

    @Test
    public void testIf_001() {
        configureFromResource("/errors/if/001.concord.yml");

        inspection()
                .assertExpressionExpected()
                .assertMissingKey("then")
                .check();
    }

    @Test
    public void testIf_002() {
        configureFromResource("/errors/if/002.concord.yml");

        inspection()
                .assertMissingKey("then")
                .check();
    }

    @Test
    public void testIf_003() {
        configureFromResource("/errors/if/003.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testIf_004() {
        configureFromResource("/errors/if/004.concord.yml");

        inspection()
                .assertUnknownKey("el")
                .check();
    }

    @Test
    public void testIf_005() {
        configureFromResource("/errors/if/005.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testIf_006() {
        configureFromResource("/errors/if/006.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testIf_007() {
        configureFromResource("/errors/if/007.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testIf_008() {
        configureFromResource("/errors/if/008.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testSwitch_000() {
        configureFromResource("/errors/switch/000.concord.yml");

        inspection()
                .assertValueRequired()
                .assertHasError(ConcordBundle.message("SwitchStepMetaType.error.missing.labels"))
                .check();
    }

    @Test
    public void testSwitch_001() {
        configureFromResource("/errors/switch/001.concord.yml");

        inspection()
                .assertHasError(ConcordBundle.message("SwitchStepMetaType.error.missing.labels"))
                .check();
    }

    @Test
    public void testSwitch_002() {
        configureFromResource("/errors/switch/002.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testSwitch_003() {
        configureFromResource("/errors/switch/003.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testPublicFlows_000() {
        configureFromResource("/errors/publicFlows/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testPublicFlows_001() {
        configureFromResource("/errors/publicFlows/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testPublicFlows_002() {
        configureFromResource("/errors/publicFlows/002.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testScript_000() {
        configureFromResource("/errors/scripts/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testScript_001() {
        configureFromResource("/errors/scripts/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testScript_002() {
        configureFromResource("/errors/scripts/002.concord.yml");

        inspection()
                .assertUnknownKey("body1")
                .check();
    }

    @Test
    public void testScript_003() {
        configureFromResource("/errors/scripts/003.concord.yml");

        inspection()
                .assertUnknownStep()
                .check();
    }

    @Test
    public void tesResources_000() {
        configureFromResource("/errors/resources/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void tesResources_001() {
        configureFromResource("/errors/resources/001.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void tesResources_002() {
        configureFromResource("/errors/resources/002.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void tesSetVariables_000() {
        configureFromResource("/errors/setVariables/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void tesSetVariables_001() {
        configureFromResource("/errors/setVariables/001.concord.yml");

        inspection()
                .assertUnknownKey("meta1")
                .check();
    }

    @Test
    public void tesSteps_000() {
        configureFromResource("/errors/steps/000.concord.yml");

        inspection()
                .assertUnknownStep()
                .check();
    }

    @Test
    public void tesFlowCallInputParams_000() {
        configureFromResource("/errors/flowCallInputParams/000.concord.yml");

        inspection()
                .assertInvalidValue(STRING_OR_EXPRESSION)
                .assertInvalidValue(BOOLEAN_OR_EXPRESSION)
                .assertInvalidValue(NUMBER_OR_EXPRESSION)
                .assertInvalidValue(ARRAY_OR_EXPRESSION)
                .assertInvalidValue(OBJECT_OR_EXPRESSION)
                .assertInvalidValue(STRING_OR_EXPRESSION)
                .check();
    }

    private Inspection inspection() {
        return new Inspection(myFixture);
    }

    private static class Inspection {

        private final CodeInsightTestFixture fixture;

        private final List<String> errors = new ArrayList<>();

        public Inspection(CodeInsightTestFixture fixture) {
            this.fixture = fixture;
        }

        public Inspection assertHasError(String message) {
            errors.add(message);
            return this;
        }

        private Inspection assertValueRequired() {
            errors.add("Value is required");
            return this;
        }

        private Inspection assertArrayRequired() {
            errors.add("Array is required");
            return this;
        }

        private Inspection assertObjectRequired() {
            errors.add(ConcordBundle.message("ConcordMetaType.error.object.is.required"));
            return this;
        }

        private Inspection assertUndefinedFlow() {
            errors.add(ConcordBundle.message("CallStepMetaType.error.undefinedFlow"));
            return this;
        }

        private Inspection assertStringValueExpected() {
            errors.add("String value expected");
            return this;
        }

        private Inspection assertIntExpected() {
            errors.add("Integer value expected");
            return this;
        }

        private Inspection assertBooleanExpected() {
            errors.add("Boolean value expected");
            return this;
        }

        private Inspection assertSingleValueExpected() {
            errors.add("Single value is expected");
            return this;
        }

        private Inspection assertExpressionExpected() {
            errors.add(ConcordBundle.message("ExpressionType.error.invalid.value"));
            return this;
        }

        private Inspection assertDurationExpected() {
            errors.add(ConcordBundle.message("DurationType.error.scalar.value"));
            return this;
        }

        private Inspection assertUnknownKey(String key) {
            errors.add(ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", key));
            return this;
        }

        private Inspection assertUnexpectedValue(String value) {
            errors.add(ConcordBundle.message("YamlEnumType.validation.error.value.unknown", value));
            return this;
        }

        private Inspection assertMissingKey(String key) {
            errors.add("Missing required key(s): '" + key + "'");
            return this;
        }

        private Inspection assertUnexpectedKey(String key) {
            errors.add(ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", key));
            return this;
        }

        public Inspection assertUnknownStep() {
            errors.add(ConcordBundle.message("StepElementMetaType.error.unknown.step"));
            return this;
        }

        public Inspection assertInvalidValue(AnyOfType type) {
            errors.add(ConcordBundle.message("invalid.value", type.expectedString()));
            return this;
        }

        public void check() {
            List<HighlightInfo> highlighting = new ArrayList<>(fixture.doHighlighting().stream()
                    .filter(highlightInfo -> highlightInfo.getSeverity() == HighlightSeverity.ERROR)
                    .toList());

            assertEquals(dump(highlighting) + "\n", errors.size(), highlighting.size());

            for (String error : errors) {
                boolean hasError = false;
                for (Iterator<HighlightInfo> it = highlighting.iterator(); it.hasNext(); ) {
                    HighlightInfo h = it.next();
                    if (h.getDescription() != null && h.getDescription().startsWith(error)) {
                        it.remove();
                        hasError = true;
                        break;
                    }
                }

                if (!hasError) {
                    fail(dump(highlighting) + "\n '" + error + "' not found\n");
                }
            }

            assertTrue(dump(highlighting), highlighting.isEmpty());
        }
    }
}
