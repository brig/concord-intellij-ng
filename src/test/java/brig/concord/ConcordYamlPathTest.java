package brig.concord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcordYamlPathTest extends ConcordYamlTestBaseJunit5 {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        configureFromText("test.concord.yml", """
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
    }

    @Test
    public void testResolveKeyText() {
        assertEquals("flows", yamlPath.keyText("flows"));
        assertEquals("main", yamlPath.keyText("flows/main"));
        assertEquals("task", yamlPath.keyText("flows/main[0]/task"));
        assertEquals("in", yamlPath.keyText("flows/main[0]/in"));
        assertEquals("times", yamlPath.keyText("flows/main[0]/retry/times"));
    }

    @Test
    public void testResolveKeyOffsets() {
        var a = yamlPath.keyStartOffset("flows/main[0]/in");
        var b = yamlPath.keyStartOffset("/flows/main[0]/in/");
        assertEquals(a, b);

        // sanity check: extracted text at that offset is "in"
        var doc = myFixture.getEditor().getDocument().getText();
        assertEquals("in", doc.substring(a, a + 2));
    }

    @Test
    public void testValueRangesAndScalarValues() {
        // url value
        var urlRange = yamlPath.valueRange("flows/main[0]/in/url");
        var urlText = myFixture.getEditor().getDocument().getText(urlRange);
        assertEquals("\"https://example.com\"", urlText); // PSI range includes quotes

        // retry times value
        var timesRange = yamlPath.valueRange("flows/main[0]/retry/times");
        var timesText = myFixture.getEditor().getDocument().getText(timesRange);
        assertEquals("3", timesText.trim());

        // meta segmentName value
        var segRange = yamlPath.valueRange("flows/main[0]/meta/segmentName");
        var segText = myFixture.getEditor().getDocument().getText(segRange);
        assertEquals("\"API Call\"", segText);
    }
}
