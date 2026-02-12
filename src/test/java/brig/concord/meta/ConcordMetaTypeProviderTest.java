package brig.concord.meta;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.meta.model.*;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.call.CallStepMetaType;
import brig.concord.meta.model.value.BooleanMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.psi.ConcordFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcordMetaTypeProviderTest extends ConcordYamlTestBaseJunit5 {

    private ConcordMetaTypeProvider provider() {
        return ConcordMetaTypeProvider.getInstance(getProject());
    }

    // --- Group 1: Root-level key resolution ---

    @Test
    void flowsRootKey() {
        configureFromText("""
                flows:
                  default:
                    - log: "hello"
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/flows").asKeyValue());
        assertSame(FlowsMetaType.getInstance(), type);
    }

    @Test
    void configurationRootKey() {
        configureFromText("""
                configuration:
                  entryPoint: default
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/configuration").asKeyValue());
        assertSame(ConfigurationMetaType.getInstance(), type);
    }

    @Test
    void profilesRootKey() {
        configureFromText("""
                profiles:
                  myProfile:
                    configuration:
                      entryPoint: default
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/profiles").asKeyValue());
        assertSame(ProfilesMetaType.getInstance(), type);
    }

    @Test
    void triggersRootKey() {
        configureFromText("""
                triggers:
                  - github:
                      entryPoint: onPush
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/triggers").asKeyValue());
        // array unwrap: TriggersMetaType -> TriggerElementMetaType
        assertSame(TriggerElementMetaType.getInstance(), type);
    }

    @Test
    void importsRootKey() {
        configureFromText("""
                imports:
                  - git:
                      url: "https://example.com"
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/imports").asKeyValue());
        // array unwrap: ImportsMetaType -> ImportElementMetaType
        assertSame(ImportElementMetaType.getInstance(), type);
    }

    @Test
    void publicFlowsRootKey() {
        configureFromText("""
                publicFlows:
                  - default
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/publicFlows").asKeyValue());
        // array unwrap: StringArrayMetaType -> StringMetaType
        assertSame(StringMetaType.getInstance(), type);
    }

    // --- Group 2: Step type resolution (dynamic identity-based) ---

    @Test
    void taskStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - task: myTask
                """);
        assertSame(TaskStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]")));
    }

    @Test
    void callStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - call: otherFlow
                """);
        assertSame(CallStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]")));
    }

    @Test
    void ifStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - if: "${condition}"
                      then:
                        - log: "yes"
                """);
        assertSame(IfStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]")));
    }

    @Test
    void exprStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - expr: "${myExpression}"
                """);
        assertSame(ExprStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]")));
    }

    @Test
    void logStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "message"
                """);
        assertSame(LogStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]")));
    }

    @Test
    void setStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - set:
                        myVar: "value"
                """);
        assertSame(SetStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]")));
    }

    @Test
    void parallelStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - parallel:
                        - log: "a"
                        - log: "b"
                """);
        assertSame(ParallelStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]")));
    }

    // --- Group 3: Nested/deeper resolution ---

    @Test
    void configurationEntryPoint() {
        configureFromText("""
                configuration:
                  entryPoint: default
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/configuration/entryPoint").asKeyValue());
        assertSame(CallMetaType.getInstance(), type);
    }

    @Test
    void configurationDebug() {
        configureFromText("""
                configuration:
                  debug: true
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/configuration/debug").asKeyValue());
        assertSame(BooleanMetaType.getInstance(), type);
    }

    @Test
    void stepInsideIfThenBlock() {
        configureFromText("""
                flows:
                  myFlow:
                    - if: "${cond}"
                      then:
                        - task: innerTask
                """);
        assertSame(TaskStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]/then/[0]")));
    }

    @Test
    void stepInsideTryBlock() {
        configureFromText("""
                flows:
                  myFlow:
                    - try:
                        - log: "in try"
                """);
        assertSame(LogStepMetaType.getInstance(), provider().getResolvedMetaType(element("/flows/myFlow/[0]/try/[0]")));
    }

    // --- Group 4: Edge cases ---

    @Test
    void documentRootResolvesToFileMetaType() {
        var yaml = configureFromText("""
                flows:
                  default:
                    - log: "hello"
                """);
        var topValue = yaml.getDocuments().getFirst().getTopLevelValue();
        assertNotNull(topValue);
        var type = provider().getResolvedMetaType(topValue);
        assertSame(ConcordFileMetaType.getInstance(), type);
    }

    @Test
    void nonConcordFileReturnsNull() {
        var file = myFixture.configureByText("test.yml", "key: value");
        assertFalse(file instanceof ConcordFile);
        var element = file.findElementAt(0);
        assertNotNull(element);
        assertNull(provider().getResolvedMetaType(element));
    }

    @Test
    void resolveFromKeyElement() {
        var yaml = configureFromText("""
                flows:
                  myFlow:
                    - task: myTask
                """);
        // findElementAt returns the leaf PsiElement of the key text.
        // DynamicMetaType.resolve() only works with YAMLMapping, not leaf elements,
        // so the step type is not resolved — StepElementMetaType is returned instead.
        var offset = yaml.getText().indexOf("task:");
        var psi = yaml.findElementAt(offset);
        assertNotNull(psi);
        assertEquals("task", psi.getText());
        var type = provider().getResolvedMetaType(psi);
        assertSame(StepElementMetaType.getInstance(), type);
    }

    @Test
    void unknownRootKeyResolvesToNull() {
        configureFromText("""
                unknownKey:
                  value: 123
                """);
        var type = provider().getResolvedKeyValueMetaTypeMeta(key("/unknownKey").asKeyValue());
        assertNull(type);
    }
}
