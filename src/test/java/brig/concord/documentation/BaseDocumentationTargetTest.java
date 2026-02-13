package brig.concord.documentation;

import brig.concord.ConcordBundle;
import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.platform.backend.documentation.DocumentationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import static brig.concord.ConcordBundle.BUNDLE;
import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseDocumentationTargetTest extends ConcordYamlTestBaseJunit5 {

    private final ConcordDocumentationTargetProvider provider = new ConcordDocumentationTargetProvider();

    protected void assertNoDocTarget(AbstractTarget path) {
        var targets = provider.documentationTargets(myFixture.getFile(), path.range().getStartOffset());
        assertEquals(0, targets.size());
    }

    protected ConcordDocumentationTarget assertDocTarget(AbstractTarget path,
                                                         @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                                         String htmlResource) {
        return assertDocTargetRaw(path, ConcordBundle.message(key), htmlResource);
    }

    protected ConcordDocumentationTarget assertDocTargetRaw(AbstractTarget path,
                                                             @NotNull String expectedHint,
                                                             String htmlResource) {
        var targets = provider.documentationTargets(myFixture.getFile(), path.range().getStartOffset());
        assertEquals(1, targets.size());

        var target = targets.getFirst();
        assertNotNull(target);

        var htmlDoc = target.computeDocumentation();
        assertNotNull(htmlDoc);
        if (htmlResource != null) {
            assertInstanceOf(DocumentationData.class, htmlDoc);
            var expectedHtml = loadResource(htmlResource);
            var actualHtml = ((DocumentationData) htmlDoc).getHtml();

            assertEquals(
                    expectedHtml.replaceAll("\\s+", "").trim(),
                    actualHtml.replaceAll("\\s+", "").trim(),
                    actualHtml
            );
        }
        assertNotNull(target.computeDocumentationHint());
        assertNotNull(target.computePresentation());
        assertEquals(expectedHint, target.computeDocumentationHint());

        return target;
    }
}
