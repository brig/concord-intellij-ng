package brig.concord.inspection;

import brig.concord.ConcordYamlTestBase;
import brig.concord.assertions.InspectionAssertions;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;

public abstract class InspectionTestBase extends ConcordYamlTestBase {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(enabledInspections());
    }

    protected abstract Collection<Class<? extends LocalInspectionTool>> enabledInspections();

    protected void assertNoErrors() {
        InspectionAssertions.assertNoErrors(this.myFixture);
    }

    protected @NotNull InspectionAssertions inspection(@NotNull ConcordYamlTestBase.AbstractTarget target) {
        return new InspectionAssertions(myFixture, target);
    }
}
