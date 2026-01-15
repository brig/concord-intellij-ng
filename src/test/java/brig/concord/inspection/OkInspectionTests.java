package brig.concord.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

public class OkInspectionTests extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(MissingKeysInspection.class, UnknownKeysInspection.class, ValueInspection.class);
    }

    @Test
    public void testCheckpoint_000() {
        configureFromResource("/ok/checkpoint/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testExpression_000() {
        configureFromResource("/ok/expression/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testExpression_001() {
        configureFromResource("/ok/expression/001.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testConfiguration_000() {
        configureFromResource("/ok/configuration/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_000() {
        configureFromResource("/ok/flowCall/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_001() {
        configureFromResource("/ok/flowCall/001.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_002() {
        configureFromResource("/ok/flowCall/002.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCall_003() {
        configureFromResource("/ok/flowCall/003.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFormCall_000() {
        configureFromResource("/ok/formCall/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testForms_000() {
        configureFromResource("/ok/forms/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testGroup_000() {
        configureFromResource("/ok/group/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testIf_000() {
        configureFromResource("/ok/if/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testImports_000() {
        configureFromResource("/ok/imports/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testParallel_000() {
        configureFromResource("/ok/imports/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testProfiles_000() {
        configureFromResource("/ok/profiles/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testPublicFlows_000() {
        configureFromResource("/ok/publicFlows/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testResources_000() {
        configureFromResource("/ok/resources/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testScript_000() {
        configureFromResource("/ok/script/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testSetVariables_000() {
        configureFromResource("/ok/setVariables/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testSwitch_000() {
        configureFromResource("/ok/switch/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testTaskCall_000() {
        configureFromResource("/ok/taskCall/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testTriggers_000() {
        configureFromResource("/ok/triggers/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testReturn_000() {
        configureFromResource("/ok/return/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCallInputParams_000() {
        configureFromResource("/ok/flowCallInputParams/000.concord.yml");
        assertNoErrors();
    }

    @Test
    public void testFlowCallInputParams_001() {
        configureFromResource("/ok/flowCallInputParams/001.concord.yml");
        assertNoErrors();
    }
}
