package brig.concord.psi;

import brig.concord.yaml.psi.YAMLFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConcordYamlPathTest extends BasePlatformTestCase {

    private ConcordYamlPath path;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByText("test.concord.yml", """
            flows:
              main:
                - task: "http"
                  in:
                    url: "https://example.com"
                  out: response
                  retry:
                    times: 3
                  meta:
                    segmentName: "API Call"
            """);
        path = new ConcordYamlPath((YAMLFile) myFixture.getFile());
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testResolveKeyText() {
        assertEquals("flows", path.keyText("flows"));
        assertEquals("main", path.keyText("flows/main"));
        assertEquals("task", path.keyText("flows/main[0]/task"));
        assertEquals("in", path.keyText("flows/main[0]/in"));
        assertEquals("times", path.keyText("flows/main[0]/retry/times"));
    }

    @Test
    public void testResolveKeyOffsets() {
        var a = path.keyStartOffset("flows/main[0]/in");
        var b = path.keyStartOffset("/flows/main[0]/in/");
        assertEquals(a, b);

        // sanity check: extracted text at that offset is "in"
        var doc = myFixture.getEditor().getDocument().getText();
        assertEquals("in", doc.substring(a, a + 2));
    }

    @Test
    public void testValueRangesAndScalarValues() {
        // url value
        var urlRange = path.valueRange("flows/main[0]/in/url");
        var urlText = myFixture.getEditor().getDocument().getText(urlRange);
        assertEquals("\"https://example.com\"", urlText); // PSI range includes quotes

        // retry times value
        var timesRange = path.valueRange("flows/main[0]/retry/times");
        var timesText = myFixture.getEditor().getDocument().getText(timesRange);
        assertEquals("3", timesText.trim());

        // meta segmentName value
        var segRange = path.valueRange("flows/main[0]/meta/segmentName");
        var segText = myFixture.getEditor().getDocument().getText(segRange);
        assertEquals("\"API Call\"", segText);
    }
}
