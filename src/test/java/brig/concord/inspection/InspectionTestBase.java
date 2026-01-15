package brig.concord.inspection;

import brig.concord.ConcordYamlTestBase;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.annotation.HighlightSeverity;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class InspectionTestBase extends ConcordYamlTestBase {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(enabledInspections());
    }

    protected abstract Collection<Class<? extends LocalInspectionTool>> enabledInspections();

    protected void assertNoErrors() {
        List<HighlightInfo> highlighting = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        if (!highlighting.isEmpty()) {
            fail(dump(highlighting));
        }
    }

    protected @NotNull InspectionAssertion inspection(@NotNull ConcordYamlTestBase.AbstractTarget target) {
        return new InspectionAssertion(myFixture, target);
    }

    protected static String dump(List<HighlightInfo> highlighting) {
        return "------ highlighting ------\n"
                + highlighting.stream().map(HighlightInfo::toString).collect(Collectors.joining("\n"))
                + "\n------ highlighting ------";
    }
}
