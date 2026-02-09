package brig.concord.assertions;

import brig.concord.ConcordBundle;
import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.meta.model.value.AnyOfType;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class InspectionAssertions {

    private final CodeInsightTestFixture myFixture;
    private final @Nullable ConcordYamlTestBaseJunit5.AbstractTarget target;
    private List<HighlightInfo> cachedHighlights;

    private final List<String> expectedErrors = new ArrayList<>();

    public InspectionAssertions(CodeInsightTestFixture myFixture) {
        this.myFixture = myFixture;
        this.target = null;
    }

    public InspectionAssertions(CodeInsightTestFixture myFixture, ConcordYamlTestBaseJunit5.AbstractTarget target) {
        this.myFixture = myFixture;
        this.target = target;
    }

    public static void assertNoWarnings(CodeInsightTestFixture fixture) {
        assertNoProblems(fixture, HighlightSeverity.WARNING);
    }

    public static void assertNoErrors(CodeInsightTestFixture fixture) {
        assertNoProblems(fixture, HighlightSeverity.ERROR);
    }

    public static void assertNoProblems(CodeInsightTestFixture fixture, HighlightSeverity severity) {
        List<HighlightInfo> highlighting = fixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(severity) >= 0)
                .toList();
        if (!highlighting.isEmpty()) {
            fail(dump(highlighting));
        }
    }

    public static String dump(List<HighlightInfo> highlighting) {
        return "------ highlighting ------\n"
                + highlighting.stream().map(HighlightInfo::toString).collect(Collectors.joining("\n"))
                + "\n------ highlighting ------";
    }

    // --- target-range-based (expect*) methods ---

    public InspectionAssertions expectDuplicateKey() {
        var keyName = target.text();
        var expected = ConcordBundle.message(
                "inspection.duplicate.keys.message", keyName);

        return expectHighlight(expected);
    }

    public InspectionAssertions expectDuplicateFlowDocParam(String paramName, String section) {
        var expected = ConcordBundle.message(
                "inspection.flow.doc.duplicate.param", paramName, section);
        return expectHighlight(expected);
    }

    public InspectionAssertions expectUnknownType(String typeName) {
        var expected = ConcordBundle.message(
                "inspection.flow.doc.unknown.type", typeName);
        return expectHighlight(expected);
    }

    public InspectionAssertions expectOrphanedFlowDoc() {
        var expected = ConcordBundle.message("inspection.flow.doc.orphaned");
        return expectHighlight(expected);
    }

    public InspectionAssertions expectUnknownKeyword(String keyword) {
        var expected = ConcordBundle.message("inspection.flow.doc.unknown.keyword", keyword);
        return expectHighlight(expected);
    }

    public InspectionAssertions expectDuplicateFlowName(String flowName, String otherFile) {
        var expected = ConcordBundle.message(
                "inspection.duplicate.flow.name.message", flowName, otherFile);
        return expectHighlight(expected);
    }

    /**
     * Checks that the file contains a warning with the out of scope message anywhere.
     */
    public static void assertHasOutOfScopeWarning(CodeInsightTestFixture fixture) {
        var expected = ConcordBundle.message("inspection.out.of.scope.message");
        var infos = fixture.doHighlighting();

        var found = infos.stream()
                .anyMatch(i -> expected.equals(i.getDescription()));

        if (!found) {
            var sb = new StringBuilder();
            sb.append("Expected out of scope warning but none found.\n")
                    .append("Expected message: ").append(expected).append("\n")
                    .append("All highlights:\n");
            for (var i : infos) {
                sb.append(" - ").append(i.getDescription())
                        .append(" [").append(i.getSeverity()).append("]\n");
            }
            fail(sb.toString());
        }
    }

    public InspectionAssertions expectHighlight(String expected) {
        if (target == null) {
            throw new IllegalStateException("expectHighlight() requires a target. Use the constructor with a target parameter.");
        }

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

    public InspectionAssertions expectNoErrors() {
        return expectNoProblems(HighlightSeverity.ERROR);
    }

    public InspectionAssertions expectNoWarnings() {
        return expectNoProblems(HighlightSeverity.WARNING);
    }

    public InspectionAssertions expectNoProblems(HighlightSeverity severity) {
        var infos = findHighlightsAt(target.range()).stream()
                .filter(info -> info.getSeverity().compareTo(severity) >= 0)
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

    // --- accumulate-then-check (assert*) methods ---

    public InspectionAssertions assertHasError(String message) {
        expectedErrors.add(message);
        return this;
    }

    public InspectionAssertions assertValueRequired() {
        expectedErrors.add("Value is required");
        return this;
    }

    public InspectionAssertions assertArrayRequired() {
        expectedErrors.add("Array is required");
        return this;
    }

    public InspectionAssertions assertObjectRequired() {
        expectedErrors.add(ConcordBundle.message("ConcordMetaType.error.object.is.required"));
        return this;
    }

    public InspectionAssertions assertUndefinedFlow() {
        expectedErrors.add(ConcordBundle.message("CallStepMetaType.error.undefinedFlow"));
        return this;
    }

    public InspectionAssertions assertStringValueExpected() {
        expectedErrors.add("String value expected");
        return this;
    }

    public InspectionAssertions assertIntExpected() {
        expectedErrors.add("Integer value expected");
        return this;
    }

    public InspectionAssertions assertBooleanExpected() {
        expectedErrors.add("Boolean value expected");
        return this;
    }

    public InspectionAssertions assertSingleValueExpected() {
        expectedErrors.add("Single value is expected");
        return this;
    }

    public InspectionAssertions assertExpressionExpected() {
        expectedErrors.add(ConcordBundle.message("ExpressionType.error.invalid.value"));
        return this;
    }

    public InspectionAssertions assertDurationExpected() {
        expectedErrors.add(ConcordBundle.message("DurationType.error.scalar.value"));
        return this;
    }

    public InspectionAssertions assertUnknownKey(String key) {
        expectedErrors.add(ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", key));
        return this;
    }

    public InspectionAssertions assertUnexpectedValue(String value) {
        expectedErrors.add(ConcordBundle.message("YamlEnumType.validation.error.value.unknown", value));
        return this;
    }

    public InspectionAssertions assertMissingKey(String key) {
        expectedErrors.add("Missing required key(s): '" + key + "'");
        return this;
    }

    public InspectionAssertions assertUnexpectedKey(String key) {
        expectedErrors.add(ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", key));
        return this;
    }

    public InspectionAssertions assertUnknownStep() {
        expectedErrors.add(ConcordBundle.message("StepElementMetaType.error.unknown.step"));
        return this;
    }

    public InspectionAssertions assertInvalidValue(AnyOfType type) {
        expectedErrors.add(ConcordBundle.message("invalid.value", type.expectedString()));
        return this;
    }

    public void check() {
        if (target != null) {
            throw new IllegalStateException("check() is for file-wide verification. Use expect* methods for target-range-based assertions.");
        }

        List<HighlightInfo> highlighting = new ArrayList<>(myFixture.doHighlighting().stream()
                .filter(highlightInfo -> highlightInfo.getSeverity() == HighlightSeverity.ERROR)
                .toList());

        assertEquals(expectedErrors.size(), highlighting.size(), dump(highlighting) + "\n");

        for (String error : expectedErrors) {
            boolean hasError = false;
            for (Iterator<HighlightInfo> it = highlighting.iterator(); it.hasNext(); ) {
                HighlightInfo h = it.next();
                if (h.getDescription() != null && h.getDescription().startsWith(error)) {
                    it.remove();
                    hasError = true;
                    break;
                }
            }

            if (!hasError) {
                fail(dump(highlighting) + "\n '" + error + "' not found\n");
            }
        }

        assertTrue(highlighting.isEmpty(), dump(highlighting));
    }

    // --- private helpers ---

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
        var infos = highlighting();

        for (var i : infos) {
            if (i == null) {
                continue;
            }

            if (i.startOffset == range.getStartOffset()
                    && i.endOffset == range.getEndOffset()) {
                result.add(i);
            }
        }
        return result;
    }


    private List<HighlightInfo> highlighting() {
        if (cachedHighlights == null) {
            cachedHighlights = myFixture.doHighlighting();
        }
        return cachedHighlights;
    }
}
