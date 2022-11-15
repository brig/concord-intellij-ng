package brig.concord;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ReadAction;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public final class InspectionUtil {

    private final CodeInsightTestFixture fixture;

    public InspectionUtil(CodeInsightTestFixture fixture) {
        this.fixture = fixture;
    }

    public void assertNoErrors() {
        assertNoHighlighting(HighlightSeverity.ERROR);
    }

    public void assertNoWarnings() {
        assertNoHighlighting(HighlightSeverity.WARNING);
    }

    public void assertNoWarning(String warning) {
        assertNoHighlighting(HighlightSeverity.WARNING, warning);
    }

    public void assertNoWeakWarning(String warning) {
        assertNoHighlighting(HighlightSeverity.WEAK_WARNING, warning);
    }

    public void assertNoError(String error) {
        assertNoHighlighting(HighlightSeverity.ERROR, error);
    }

    public void assertHasError(String message) {
        assertHasHighlightingMessage(HighlightSeverity.ERROR, message);
    }

    private void assertNoHighlighting(HighlightSeverity severity, String message) {
        assertTrue("Highlighting not wanted but present: " + message, getHighlighting(severity).stream().noneMatch(
                highlightInfo -> highlightInfo.getDescription().startsWith(message)
        ));
    }

    private void assertNoHighlighting(HighlightSeverity severity) {
        final List<HighlightInfo> highlighting = getHighlighting(severity);
        if (!highlighting.isEmpty()) {
            ReadAction.run(() -> fail(String.format("All %s highlighting%n%s", severity.myName, highlighting)));
        }
    }

    private void assertHasHighlightingMessage(HighlightSeverity severity, String message) {
        assertHasHighlightingMessage(severity, message, null);
    }

    private boolean hasHighlightingDescription(HighlightInfo highlightInfo, String description, String highlightText) {
        if (highlightText != null && !highlightInfo.getText().equals(highlightText)) {
            return false;
        }
        return highlightInfo.getDescription() != null &&
                highlightInfo.getDescription().startsWith(description);
    }

    private void assertHasHighlightingMessage(HighlightSeverity severity, String message, String highlightText) {
        final List<HighlightInfo> highlighting = getHighlighting(severity);
        assertTrue(
                allHighlightingAsMessage(),
                highlighting.stream().anyMatch(
                        highlightInfo -> hasHighlightingDescription(highlightInfo, message, highlightText)
                ));
    }

    private String allHighlightingAsMessage() {
        return getHighlighting().stream()
                .map(highlightInfo -> String.format(
                        "%s: %s", highlightInfo.getSeverity().myName, highlightInfo.getDescription()
                )).collect(Collectors.joining("\n"));
    }

    private List<HighlightInfo> getHighlighting(HighlightSeverity severity) {
        return getHighlighting().stream().filter(
                highlightInfo -> highlightInfo.getSeverity() == severity
        ).collect(Collectors.toList());
    }

    private @NotNull List<HighlightInfo> getHighlighting() {
        try {
            return fixture.doHighlighting();
        } catch (IllegalStateException e) {
            throw new RuntimeException("Don't wrap highlight assertions in ReadAction.read locks");
        }
    }

}
