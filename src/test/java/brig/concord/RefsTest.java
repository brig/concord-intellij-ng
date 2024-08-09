package brig.concord;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RefsTest extends BasePlatformTestCase {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources/refs";
    }

    @Test
    public void testCallReference() {
        myFixture.configureByFile("flowCall/concord.yml");

        ReadAction.run(() -> {
            PsiElement elementAtCaret = myFixture.getElementAtCaret();
            Assertions.assertTrue(elementAtCaret instanceof YAMLKeyValue);
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
    }

    private void assertCallInParams(String file, String expectedComment) {
        myFixture.configureByFile(file);

        ReadAction.run(() -> {
            PsiElement elementAtCaret = myFixture.getElementAtCaret();
            Assertions.assertTrue(elementAtCaret instanceof PsiComment);
            PsiComment inParamDef = (PsiComment)elementAtCaret;
            Assertions.assertEquals(expectedComment, inParamDef.getText());
        });
    }
}
