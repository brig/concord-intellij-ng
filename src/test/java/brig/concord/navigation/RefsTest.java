package brig.concord.navigation;

import brig.concord.ConcordYamlTestBase;
import brig.concord.psi.FlowDocParameter;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
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
        assertCallInParams("flowCallInParams/1.concord.yml", "k1");
        assertCallInParams("flowCallInParams/2.concord.yml", "k2");
        assertCallInParams("flowCallInParams/3.concord.yml", "k3");
        assertCallInParams("flowCallInParams/4.concord.yml", "k4");
        assertCallInParams("flowCallInParams/5.concord.yml", "k5");
        assertCallInParams("flowCallInParams/6.concord.yml", "k6");
        assertCallInParams("flowCallInParams/7.concord.yml", "k 1");
    }

    private void assertCallInParams(String file, String expectedParamName) {
        configureFromResource("/refs/" + file);

        ReadAction.run(() -> {
            // Find the YAMLKeyValue at caret position
            PsiElement leafAtCaret = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
            YAMLKeyValue keyValue = PsiTreeUtil.getParentOfType(leafAtCaret, YAMLKeyValue.class);
            Assertions.assertNotNull(keyValue, "Should find YAMLKeyValue at caret");

            // Get all references from the key-value
            PsiReference[] refs = keyValue.getReferences();
            Assertions.assertTrue(refs.length > 0, "Element should have references");

            // Find reference that resolves to FlowDocParameter
            PsiElement resolved = null;
            for (PsiReference ref : refs) {
                PsiElement target = ref.resolve();
                if (target instanceof FlowDocParameter) {
                    resolved = target;
                    break;
                }
            }
            Assertions.assertNotNull(resolved, "Should resolve to FlowDocParameter");
            Assertions.assertInstanceOf(FlowDocParameter.class, resolved);

            FlowDocParameter inParamDef = (FlowDocParameter) resolved;
            Assertions.assertEquals(expectedParamName, inParamDef.getName());
        });
    }
}
