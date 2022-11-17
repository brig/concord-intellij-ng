package brig.concord;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.yaml.YAMLBundle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ErrorInspectionTests extends BaseInspectionTest {

    @Test
    public void testCheckpoint_000() {
        configureByFile("errors/checkpoint/000.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testCheckpoint_001() {
        configureByFile("errors/checkpoint/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testCheckpoint_002() {
        configureByFile("errors/checkpoint/002.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testCheckpoint_003() {
        configureByFile("errors/checkpoint/003.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testCheckpoint_004() {
        configureByFile("errors/checkpoint/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testCheckpoint_005() {
        configureByFile("errors/checkpoint/005.concord.yml");
        inspection()
                .assertUnexpectedKey("trash")
                .check();
    }

    @Test
    public void testExpression_000() {
        configureByFile("errors/expression/000.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_001() {
        configureByFile("errors/expression/001.concord.yml");
        inspection()
                .assertHasError(ConcordBundle.message("ExpressionType.error.invalid.value"))
                .check();
    }

    @Test
    public void testExpression_002() {
        configureByFile("errors/expression/002.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_003() {
        configureByFile("errors/expression/003.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testExpression_005() {
        configureByFile("errors/expression/005.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_006() {
        configureByFile("errors/expression/006.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testExpression_007() {
        configureByFile("errors/expression/007.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testExpression_008() {
        configureByFile("errors/expression/008.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    // TODO: IdentityElementMetaType.validateValue
    @Test
    public void testExpression_009() {
        configureByFile("errors/expression/009.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testExpression_010() {
        configureByFile("errors/expression/010.concord.yml");
        inspection()
                .assertUnknownStep()
                .check();
    }

    @Test
    public void testImports_001() {
        configureByFile("errors/imports/001.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testImports_002() {
        configureByFile("errors/imports/002.concord.yml");
        inspection()
                .assertArrayRequired()
                .assertUnexpectedKey("k")
                .check();
    }

    @Test
    public void testImports_002_1() {
        configureByFile("errors/imports/002_1.concord.yml");
        inspection()
                .assertUnexpectedKey("k")
                .check();
    }

    @Test
    public void testImports_003() {
        configureByFile("errors/imports/003.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testImports_004() {
        configureByFile("errors/imports/004.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testImports_005() {
        configureByFile("errors/imports/005.concord.yml");
        inspection()
                .assertValueRequired()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testImports_006() {
        configureByFile("errors/imports/006.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_007() {
        configureByFile("errors/imports/007.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_008() {
        configureByFile("errors/imports/008.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testImports_009() {
        configureByFile("errors/imports/009.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_010() {
        configureByFile("errors/imports/010.concord.yml");
        inspection()
                .assertMissingKey("name")
                .check();
    }

    @Test
    public void testImports_011() {
        configureByFile("errors/imports/011.concord.yml");
        inspection()
                .assertUnexpectedKey("git-trash")
                .check();
    }

    @Test
    public void testImports_012() {
        configureByFile("errors/imports/012.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testImports_013() {
        configureByFile("errors/imports/013.concord.yml");
        inspection()
                .assertUnexpectedKey("trash")
                .check();
    }

    @Test
    public void testImports_014() {
        configureByFile("errors/imports/014.concord.yml");
        inspection()
                .assertObjectRequired()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testImports_015() {
        configureByFile("errors/imports/015.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testImports_016() {
        configureByFile("errors/imports/016.concord.yml");
        inspection()
                .assertHasError("Valid regular expression or string required. Error: 'Unclosed character class near index 1\n" +
                        "[.\n" +
                        " ^'")
                .check();
    }

    @Test
    public void testTriggers_001() {
        configureByFile("errors/triggers/001.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_002() {
        configureByFile("errors/triggers/002.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_003() {
        configureByFile("errors/triggers/003.concord.yml");
        inspection()
                .assertMissingKey("conditions, entryPoint")
                .assertIntExpected()
                .check();
    }

    @Test
    public void testTriggers_004() {
        configureByFile("errors/triggers/004.concord.yml");
        inspection()
                .assertMissingKey("conditions, entryPoint")
                .check();
    }

    @Test
    public void testTriggers_005() {
        configureByFile("errors/triggers/005.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTriggers_006() {
        configureByFile("errors/triggers/006.concord.yml");
        inspection()
                .assertMissingKey("conditions")
                .check();
    }

    @Test
    public void testTriggers_007() {
        configureByFile("errors/triggers/007.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_008() {
        configureByFile("errors/triggers/008.concord.yml");

        inspection()
                .assertMissingKey("type")
                .assertUnexpectedKey("test")
                .check();
    }

    @Test
    public void testTriggers_009() {
        configureByFile("errors/triggers/009.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_010() {
        configureByFile("errors/triggers/010.concord.yml");
        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testTriggers_011() {
        configureByFile("errors/triggers/011.concord.yml");
        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testTriggers_012() {
        configureByFile("errors/triggers/012.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_013() {
        configureByFile("errors/triggers/013.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_014() {
        configureByFile("errors/triggers/014.concord.yml");
        inspection()
                .assertHasError("Valid regular expression or string required. Error: 'Dangling meta character '*' near index 0\n" +
                        "*\n" +
                        "^'")
                .check();
    }

    @Test
    public void testTriggers_015() {
        configureByFile("errors/triggers/015.concord.yml");
        inspection()
                .assertMissingKey("conditions, entryPoint")
                .check();
    }

    @Test
    public void testTriggers_016() {
        configureByFile("errors/triggers/016.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_017() {
        configureByFile("errors/triggers/017.concord.yml");
        inspection()
                .assertMissingKey("entryPoint")
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTriggers_018() {
        configureByFile("errors/triggers/018.concord.yml");
        inspection()
                .assertStringValueExpected()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTriggers_019() {
        configureByFile("errors/triggers/019.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTriggers_020() {
        configureByFile("errors/triggers/020.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_021() {
        configureByFile("errors/triggers/021.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_022() {
        configureByFile("errors/triggers/022.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_023() {
        configureByFile("errors/triggers/023.concord.yml");
        inspection()
                .assertMissingKey("entryPoint")
                .check();
    }

    @Test
    public void testTriggers_024() {
        configureByFile("errors/triggers/024.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("oneops")
    public void testTriggers_025() {
        configureByFile("errors/triggers/025.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("oneops")
    public void testTriggers_026() {
        configureByFile("errors/triggers/026.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("oneops")
    public void testTriggers_027() {
        configureByFile("errors/triggers/027.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    @Disabled("custom trigger")
    public void testTriggers_028() {
        configureByFile("errors/triggers/028.concord.yml");
        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testTriggers_029() {
        configureByFile("errors/triggers/029.concord.yml");
        inspection()
                .assertSingleValueExpected()
                .check();
    }

    @Test
    public void testTriggers_030() {
        configureByFile("errors/triggers/030.concord.yml");
        inspection()
                .assertHasError("Valid timezone required")
                .check();
    }

    @Test
    public void testTriggers_031() {
        configureByFile("errors/triggers/031.concord.yml");
        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testTriggers_031_1() {
        configureByFile("errors/triggers/031_1.concord.yml");
        inspection()
                .assertUnknownKey("unknown")
                .check();
    }

    @Test
    public void testTriggers_032() {
        configureByFile("errors/triggers/032.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTriggers_033() {
        configureByFile("errors/triggers/033.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTriggers_034() {
        configureByFile("errors/triggers/034.concord.yml");
        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testTriggers_035() {
        configureByFile("errors/triggers/035.concord.yml");
        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testTriggers_036() {
        configureByFile("errors/triggers/036.concord.yml");
        inspection()
                .assertUnexpectedValue("test")
                .check();
    }

    @Test
    public void testTriggers_037() {
        configureByFile("errors/triggers/037.concord.yml");
        inspection()
                .assertMissingKey("group or groupBy")
                .check();
    }

    @Test
    public void testTasks_000() {
        configureByFile("errors/tasks/000.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_001() {
        configureByFile("errors/tasks/001.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTasks_002() {
        configureByFile("errors/tasks/002.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_003() {
        configureByFile("errors/tasks/003.concord.yml");
        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTasks_005() {
        configureByFile("errors/tasks/005.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_006() {
        configureByFile("errors/tasks/006.concord.yml");
        inspection()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testTasks_007() {
        configureByFile("errors/tasks/007.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_008() {
        configureByFile("errors/tasks/008.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_009() {
        configureByFile("errors/tasks/009.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_009_1() {
        configureByFile("errors/tasks/009_1.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_009_2() {
        configureByFile("errors/tasks/009_2.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_009_3() {
        configureByFile("errors/tasks/009_3.concord.yml");
        inspection()
                .assertIntExpected()
                .check();
    }

    @Test
    public void testTasks_009_4() {
        configureByFile("errors/tasks/009_4.concord.yml");
        inspection()
                .assertUnexpectedValue("a")
                .check();
    }

    @Test
    public void testTasks_010() {
        configureByFile("errors/tasks/010.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_011() {
        configureByFile("errors/tasks/011.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_012() {
        configureByFile("errors/tasks/012.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_013() {
        configureByFile("errors/tasks/013.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_014() {
        configureByFile("errors/tasks/014.concord.yml");
        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_015() {
        configureByFile("errors/tasks/015.concord.yml");
        inspection()
                .assertUnexpectedKey("trash")
                .check();
    }

    @Test
    public void testTasks_016() {
        configureByFile("errors/tasks/016.concord.yml");
        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testTasks_017() {
        configureByFile("errors/tasks/017.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testTasks_018() {
        configureByFile("errors/tasks/018.concord.yml");

        inspection()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testTasks_019() {
        configureByFile("errors/tasks/019.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testTasks_020() {
        configureByFile("errors/tasks/020.concord.yml");

        inspection()
                .assertStringValueExpected()
                .assertUnexpectedValue("trash")
                .check();
    }

    @Test
    public void testTasks_021() {
        configureByFile("errors/tasks/021.concord.yml");

        inspection()
                .assertStringValueExpected()
                .assertIntExpected()
                .check();
    }

    @Test
    public void testFlowCall_000() {
        configureByFile("errors/flowCall/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_001() {
        configureByFile("errors/flowCall/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testFlowCall_002() {
        configureByFile("errors/flowCall/002.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_003() {
        configureByFile("errors/flowCall/003.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testFlowCall_005() {
        configureByFile("errors/flowCall/005.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_006() {
        configureByFile("errors/flowCall/006.concord.yml");

        inspection()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testFlowCall_007() {
        configureByFile("errors/flowCall/007.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_008() {
        configureByFile("errors/flowCall/008.concord.yml");

        inspection()
                .assertUnknownKey("withItems")
                .check();
    }

    @Test
    public void testFlowCall_009() {
        configureByFile("errors/flowCall/009.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_010() {
        configureByFile("errors/flowCall/010.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFlowCall_011() {
        configureByFile("errors/flowCall/011.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_012() {
        configureByFile("errors/flowCall/012.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_013() {
        configureByFile("errors/flowCall/013.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_014() {
        configureByFile("errors/flowCall/014.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFlowCall_015() {
        configureByFile("errors/flowCall/015.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testFlowCall_016() {
        configureByFile("errors/flowCall/016.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlowCall_017() {
        configureByFile("errors/flowCall/017.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFlowCall_018() {
        configureByFile("errors/flowCall/018.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testFlowCall_019() {
        configureByFile("errors/flowCall/019.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testGroup_000() {
        configureByFile("errors/group/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_001() {
        configureByFile("errors/group/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testGroup_002() {
        configureByFile("errors/group/002.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testGroup_003() {
        configureByFile("errors/group/003.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testGroup_004() {
        configureByFile("errors/group/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_005() {
        configureByFile("errors/group/005.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_006() {
        configureByFile("errors/group/006.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testGroup_007() {
        configureByFile("errors/group/007.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testGroup_008() {
        configureByFile("errors/group/008.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testParallel_000() {
        configureByFile("errors/parallel/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testParallel_001() {
        configureByFile("errors/parallel/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testParallel_002() {
        configureByFile("errors/parallel/002.concord.yml");

        inspection()
                .assertUnknownStep()
                .check();
    }

    @Test
    public void testParallel_003() {
        configureByFile("errors/parallel/003.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testParallel_004() {
        configureByFile("errors/parallel/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testParallel_005() {
        configureByFile("errors/parallel/005.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testForms_000() {
        configureByFile("errors/forms/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testForms_001() {
        configureByFile("errors/forms/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testForms_002() {
        configureByFile("errors/forms/002.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testForms_003() {
        configureByFile("errors/forms/003.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testForms_004() {
        configureByFile("errors/forms/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testForms_005() {
        configureByFile("errors/forms/005.concord.yml");

        inspection()
                .assertUnexpectedKey("error")
                .check();
    }

    @Test
    public void testForms_006() {
        configureByFile("errors/forms/006.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testConfiguration_000() {
        configureByFile("errors/configuration/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_001() {
        configureByFile("errors/configuration/001.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_002() {
        configureByFile("errors/configuration/002.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testConfiguration_003() {
        configureByFile("errors/configuration/003.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }
    @Test
    public void testConfiguration_004() {
        configureByFile("errors/configuration/004.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testConfiguration_005() {
        configureByFile("errors/configuration/005.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_005_1() {
        configureByFile("errors/configuration/005_1.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testConfiguration_006() {
        configureByFile("errors/configuration/006.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testConfiguration_007() {
        configureByFile("errors/configuration/007.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_008() {
        configureByFile("errors/configuration/008.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_009() {
        configureByFile("errors/configuration/009.concord.yml");

        inspection()
                .assertDurationExpected()
                .check();
    }

    @Test
    public void testConfiguration_010() {
        configureByFile("errors/configuration/010.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_011() {
        configureByFile("errors/configuration/011.concord.yml");

        inspection()
                .assertMissingKey("group")
                .check();
    }

    @Test
    public void testConfiguration_011_1() {
        configureByFile("errors/configuration/011_1.concord.yml");

        inspection()
                .assertHasError(ConcordBundle.message("StringType.error.empty.scalar.value"))
                .check();
    }

    @Test
    public void testConfiguration_012() {
        configureByFile("errors/configuration/012.concord.yml");

        inspection()
                .assertUnknownKey("mode1")
                .check();
    }

    @Test
    public void testConfiguration_013() {
        configureByFile("errors/configuration/013.concord.yml");

        inspection()
                .assertUnexpectedValue("canceL")
                .check();
    }

    @Test
    public void testConfiguration_014() {
        configureByFile("errors/configuration/014.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testConfiguration_015() {
        configureByFile("errors/configuration/015.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_016() {
        configureByFile("errors/configuration/016.concord.yml");

        inspection()
                .assertUnexpectedValue("1")
                .check();
    }

    @Test
    public void testConfiguration_017() {
        configureByFile("errors/configuration/017.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_018() {
        configureByFile("errors/configuration/018.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testConfiguration_019() {
        configureByFile("errors/configuration/019.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_020() {
        configureByFile("errors/configuration/020.concord.yml");

        inspection()
                .assertSingleValueExpected()
                .assertSingleValueExpected()
                .check();
    }

    @Test
    public void testConfiguration_021() {
        configureByFile("errors/configuration/021.concord.yml");

        inspection()
                .assertUnexpectedValue("1")
                .check();
    }

    @Test
    public void testConfiguration_022() {
        configureByFile("errors/configuration/022.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testConfiguration_023() {
        configureByFile("errors/configuration/023.concord.yml");

        inspection()
                .assertDurationExpected()
                .check();
    }

    @Test
    public void testForms_007() {
        configureByFile("errors/forms/007.concord.yml");

        inspection()
                .assertUnexpectedValue("123")
                .check();
    }

    @Test
    public void testForms_008() {
        configureByFile("errors/forms/008.concord.yml");

        inspection()
                .assertMissingKey("type")
                .check();
    }

    @Test
    public void testForms_009() {
        configureByFile("errors/forms/009.concord.yml");

        inspection()
                .assertUnexpectedValue("123")
                .check();
    }

    @Test
    public void testForms_010() {
        configureByFile("errors/forms/010.concord.yml");

        inspection()
                .assertUnexpectedValue("1")
                .check();
    }

    @Test
    public void testForms_011() {
        configureByFile("errors/forms/011.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testForms_012() {
        configureByFile("errors/forms/012.concord.yml");

        inspection()
                .assertUnexpectedKey("min")
                .assertUnexpectedKey("max")
                .check();
    }

    @Test
    public void testFormCall_000() {
        configureByFile("errors/formCall/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFormCall_001() {
        configureByFile("errors/formCall/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testFormCall_002() {
        configureByFile("errors/formCall/002.concord.yml");

        inspection()
                .assertUnexpectedKey("a")
                .assertUnexpectedKey("b")
                .check();
    }

    @Test
    public void testFormCall_003() {
        configureByFile("errors/formCall/003.concord.yml");

        inspection()
                .assertBooleanExpected()
                .check();
    }

    @Test
    public void testFormCall_004() {
        configureByFile("errors/formCall/004.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFormCall_005() {
        configureByFile("errors/formCall/005.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFormCall_006() {
        configureByFile("errors/formCall/006.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFormCall_007() {
        configureByFile("errors/formCall/007.concord.yml");

        inspection()
                .assertExpressionExpected()
                .check();
    }

    @Test
    public void testFlows_000() {
        configureByFile("errors/flows/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testFlows_001() {
        configureByFile("errors/flows/001.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testFlows_002() {
        configureByFile("errors/flows/002.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testProfiles_000() {
        configureByFile("errors/profiles/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testProfiles_001() {
        configureByFile("errors/profiles/001.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testProfiles_002() {
        configureByFile("errors/profiles/002.concord.yml");

        inspection()
                .assertObjectRequired()
                .check();
    }

    @Test
    public void testProfiles_003() {
        configureByFile("errors/profiles/003.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testIf_000() {
        configureByFile("errors/if/000.concord.yml");

        inspection()
                .assertValueRequired()
                .assertMissingKey("then")
                .check();
    }

    @Test
    public void testIf_001() {
        configureByFile("errors/if/001.concord.yml");

        inspection()
                .assertExpressionExpected()
                .assertMissingKey("then")
                .check();
    }

    @Test
    public void testIf_002() {
        configureByFile("errors/if/002.concord.yml");

        inspection()
                .assertMissingKey("then")
                .check();
    }

    @Test
    public void testIf_003() {
        configureByFile("errors/if/003.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testIf_004() {
        configureByFile("errors/if/004.concord.yml");

        inspection()
                .assertUnknownKey("el")
                .check();
    }

    @Test
    public void testIf_005() {
        configureByFile("errors/if/005.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testIf_006() {
        configureByFile("errors/if/006.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void testIf_007() {
        configureByFile("errors/if/007.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testSwitch_000() {
        configureByFile("errors/switch/000.concord.yml");

        inspection()
                .assertValueRequired()
                .assertHasError(ConcordBundle.message("SwitchStepMetaType.error.missing.labels"))
                .check();
    }

    @Test
    public void testSwitch_001() {
        configureByFile("errors/switch/001.concord.yml");

        inspection()
                .assertHasError(ConcordBundle.message("SwitchStepMetaType.error.missing.labels"))
                .check();
    }

    @Test
    public void testSwitch_002() {
        configureByFile("errors/switch/002.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testSwitch_003() {
        configureByFile("errors/switch/003.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testPublicFlows_000() {
        configureByFile("errors/publicFlows/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testPublicFlows_001() {
        configureByFile("errors/publicFlows/001.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void testPublicFlows_002() {
        configureByFile("errors/publicFlows/002.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testScript_000() {
        configureByFile("errors/scripts/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void testScript_001() {
        configureByFile("errors/scripts/001.concord.yml");

        inspection()
                .assertStringValueExpected()
                .check();
    }

    @Test
    public void testScript_002() {
        configureByFile("errors/scripts/002.concord.yml");

        inspection()
                .assertUnknownKey("body1")
                .check();
    }

    @Test
    public void testScript_003() {
        configureByFile("errors/scripts/003.concord.yml");

        inspection()
                .assertUnknownStep()
                .check();
    }

    @Test
    public void tesResources_000() {
        configureByFile("errors/resources/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void tesResources_001() {
        configureByFile("errors/resources/001.concord.yml");

        inspection()
                .assertUnknownKey("trash")
                .check();
    }

    @Test
    public void tesResources_002() {
        configureByFile("errors/resources/002.concord.yml");

        inspection()
                .assertArrayRequired()
                .check();
    }

    @Test
    public void tesSetVariables_000() {
        configureByFile("errors/setVariables/000.concord.yml");

        inspection()
                .assertValueRequired()
                .check();
    }

    @Test
    public void tesSetVariables_001() {
        configureByFile("errors/setVariables/001.concord.yml");

        inspection()
                .assertUnknownKey("meta1")
                .check();
    }

    @Test
    public void tesSteps_000() {
        configureByFile("errors/steps/000.concord.yml");

        inspection()
                .assertUnknownStep()
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
            errors.add(YAMLBundle.message("YamlUnknownKeysInspectionBase.unknown.key", key));
            return this;
        }

        private Inspection assertUnexpectedValue(String value) {
            errors.add(YAMLBundle.message("YamlEnumType.validation.error.value.unknown", value));
            return this;
        }

        private Inspection assertMissingKey(String key) {
            errors.add("Missing required key(s): '" + key + "'");
            return this;
        }

        private Inspection assertUnexpectedKey(String key) {
            errors.add(YAMLBundle.message("YamlUnknownKeysInspectionBase.unknown.key", key));
            return this;
        }

        public Inspection assertUnknownStep() {
            errors.add(ConcordBundle.message("StepElementMetaType.error.unknown.step"));
            return this;
        }

        public void check() {
            List<HighlightInfo> highlighting = new ArrayList<>(fixture.doHighlighting());
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
