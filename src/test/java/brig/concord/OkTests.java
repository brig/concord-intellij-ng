package brig.concord;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class OkTests extends BaseTest {

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

    private void assertNoErrors() {
        List<HighlightInfo> highlighting = myFixture.doHighlighting();
        if (!highlighting.isEmpty()) {
            fail(String.format("highlighting%n%s", highlighting));
        }
    }
}
