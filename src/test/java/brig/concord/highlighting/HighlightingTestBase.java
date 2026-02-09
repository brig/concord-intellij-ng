package brig.concord.highlighting;

import brig.concord.ConcordYamlTestBaseJunit5;
import org.jetbrains.annotations.NotNull;

public abstract class HighlightingTestBase extends ConcordYamlTestBaseJunit5 {

    protected @NotNull HighlightAssertion highlight(@NotNull ConcordYamlTestBaseJunit5.AbstractTarget target) {
        return new HighlightAssertion(myFixture, target);
    }
}
