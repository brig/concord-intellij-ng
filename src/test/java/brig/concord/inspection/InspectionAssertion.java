package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.ConcordYamlTestBase;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class InspectionAssertion {

    private final CodeInsightTestFixture myFixture;
    private final ConcordYamlTestBase.AbstractTarget target;

    public InspectionAssertion(CodeInsightTestFixture myFixture, ConcordYamlTestBase.AbstractTarget target) {
        this.myFixture = myFixture;
        this.target = target;
    }

    public InspectionAssertion expectDuplicateKey() {
        var keyName = target.text();
        var expected = ConcordBundle.message(
                "inspection.duplicate.keys.message", keyName);

        return expectHighlight(expected);
    }

    public InspectionAssertion expectHighlight(String expected) {
        var infos = assertHighlightAtRange(target.range());

        var ok = infos.stream()
                .anyMatch(i -> expected.equals(i.getDescription()));

        if (!ok) {
            var sb = new StringBuilder();
            sb.append("No highlight with expected message found.\n")
                    .append("Expected: ").append(expected).append("\n")
                    .append("Actual highlights:\n");

            for (var i : infos) {
                sb.append(" - ")
                        .append(i.getDescription())
                        .append(" [").append(i.getSeverity()).append("]")
                        .append(", from ").append(i.getToolId()).append("\n");
            }

            throw new AssertionError(sb.toString());
        }

        return this;
    }

    public InspectionAssertion expectNoProblems() {
        var infos = findHighlightsAt(target.range()).stream()
                .filter(info -> info.getSeverity() == HighlightSeverity.ERROR)
                .toList();
        if (!infos.isEmpty()) {
            var sb = new StringBuilder();
            sb.append("Expected no problems at ").append(target.range())
                    .append(" but found:\n");
            for (var i : infos) {
                sb.append(" - ").append(i.getDescription())
                        .append(" [").append(i.getSeverity()).append("]\n");
            }
            fail(sb.toString());
        }
        return this;
    }

    private @NotNull List<HighlightInfo> assertHighlightAtRange(@NotNull TextRange range) {
        var result = findHighlightsAt(range);
        if (result.isEmpty()) {
            throw new AssertionError(
                    "No highlights found at " + target.range() +
                            " for target '" + target.text() +
                            "' (path=" + target.path() + ")\n\n"
            );
        }
        return result;
    }

    private @NotNull List<HighlightInfo> findHighlightsAt(@NotNull TextRange range) {
        var result = new ArrayList<HighlightInfo>();
        var infos = myFixture.doHighlighting();

        for (var i : infos) {
            if (i == null) continue;

            if (i.startOffset == range.getStartOffset()
                    && i.endOffset == range.getEndOffset()) {
                result.add(i);
            }
        }
        return result;
    }
}
