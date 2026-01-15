package brig.concord.inspection;

import brig.concord.ConcordYamlTestBase;
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

    protected @NotNull InspectionAssertion inspection(@NotNull ConcordYamlTestBase.AbstractTarget target) {
        return new InspectionAssertion(myFixture, target);
    }
}
