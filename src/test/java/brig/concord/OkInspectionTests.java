package brig.concord;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class OkInspectionTests extends BaseInspectionTest {

    @Test
    public void testCheckpoint_000() {
        configureByFile("ok/checkpoint/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testExpression_000() {
        configureByFile("ok/expression/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testExpression_001() {
        configureByFile("ok/expression/001.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testConfiguration_000() {
        configureByFile("ok/configuration/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_000() {
        configureByFile("ok/flowCall/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_001() {
        configureByFile("ok/flowCall/001.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_002() {
        configureByFile("ok/flowCall/002.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_003() {
        configureByFile("ok/flowCall/003.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFormCall_000() {
        configureByFile("ok/formCall/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testForms_000() {
        configureByFile("ok/forms/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testGroup_000() {
        configureByFile("ok/group/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testIf_000() {
        configureByFile("ok/if/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testImports_000() {
        configureByFile("ok/imports/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testParallel_000() {
        configureByFile("ok/imports/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testProfiles_000() {
        configureByFile("ok/profiles/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testPublicFlows_000() {
        configureByFile("ok/publicFlows/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testResources_000() {
        configureByFile("ok/resources/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testScript_000() {
        configureByFile("ok/script/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testSetVariables_000() {
        configureByFile("ok/setVariables/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testSwitch_000() {
        configureByFile("ok/switch/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testTaskCall_000() {
        configureByFile("ok/taskCall/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testTriggers_000() {
        configureByFile("ok/triggers/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testReturn_000() {
        configureByFile("ok/return/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCallInputParams_000() {
        configureByFile("ok/flowCallInputParams/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCallInputParams_001() {
        configureByFile("ok/flowCallInputParams/001.concord.yml");
        assertNoErrors();
    }

    private void assertNoErrors() {
        List<HighlightInfo> highlighting = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity() == HighlightSeverity.ERROR)
                .toList();
        if (!highlighting.isEmpty()) {
            fail(dump(highlighting));
        }
    }
}
