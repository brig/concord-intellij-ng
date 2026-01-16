package brig.concord.navigation;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import brig.concord.yaml.psi.YAMLKeyValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RefsTest extends ConcordYamlTestBase {

    @Test
    public void testCallReference() {
        configureFromResource("/refs/flowCall/concord.yml");

        ReadAction.run(() -> {
            PsiElement elementAtCaret = myFixture.getElementAtCaret();
            Assertions.assertInstanceOf(YAMLKeyValue.class, elementAtCaret);
            YAMLKeyValue flowDef = (YAMLKeyValue)elementAtCaret;
            assertNotNull(flowDef);
            Assertions.assertEquals("test", flowDef.getKeyText());
        });
    }

    @Test
    public void testCallInParamsReference() {
        assertCallInParams("flowCallInParams/1.concord.yml", "#   k1: string, mandatory, k1 definition");
        assertCallInParams("flowCallInParams/2.concord.yml", "#   k2: boolean, k2 definition");
        assertCallInParams("flowCallInParams/3.concord.yml", "#   k3: number, k3 definition");
        assertCallInParams("flowCallInParams/4.concord.yml", "#   k4: array, k4 definition");
        assertCallInParams("flowCallInParams/5.concord.yml", "#   k5: object, k5 definition");
        assertCallInParams("flowCallInParams/6.concord.yml", "#   k6: k6 definition");
        assertCallInParams("flowCallInParams/7.concord.yml", "#   k 1: string, k1 definition");
    }

    private void assertCallInParams(String file, String expectedComment) {
        configureFromResource("/refs/" + file);

        ReadAction.run(() -> {
            PsiElement elementAtCaret = myFixture.getElementAtCaret();
            Assertions.assertInstanceOf(PsiComment.class, elementAtCaret);
            PsiComment inParamDef = (PsiComment)elementAtCaret;
            Assertions.assertEquals(expectedComment, inParamDef.getText());
        });
    }
}
