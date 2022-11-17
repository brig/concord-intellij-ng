package brig.concord;

import brig.concord.psi.YamlPsiUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        myFixture.configureByFile("concord.yml");

        ReadAction.run(() -> {
            PsiElement elementAtCaret = myFixture.getElementAtCaret();
            Assertions.assertTrue(elementAtCaret instanceof YAMLSequence);
            YAMLKeyValue flowDef = YamlPsiUtils.getParentOfType(elementAtCaret, YAMLKeyValue.class, true);
            assertNotNull(flowDef);
            Assertions.assertEquals("test", flowDef.getKeyText());
        });
    }
}
