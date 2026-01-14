package brig.concord.highlighting;

import brig.concord.ConcordYamlTestBase;
import org.jetbrains.annotations.NotNull;

public abstract class HighlightingTestBase extends ConcordYamlTestBase {

    protected @NotNull HighlightAssertion highlight(@NotNull ConcordYamlTestBase.AbstractTarget target) {
        return new HighlightAssertion(myFixture, target);
    }
}
