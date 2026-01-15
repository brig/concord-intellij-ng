package brig.concord.completion;

import brig.concord.ConcordYamlTestBase;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Test;

public class CompletionTest extends ConcordYamlTestBase {

    @Test
    public void testCompletion() {
        configureFromResource("/completion/00.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "p1");
    }

    @Test
    public void testCompletionSteps() {
        configureFromResource("/completion/01.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "block", "call", "checkpoint", "exit", "expr", "form", "if", "log", "logYaml", "parallel", "return", "script", "set", "suspend", "switch", "task", "throw", "try");
    }
}
