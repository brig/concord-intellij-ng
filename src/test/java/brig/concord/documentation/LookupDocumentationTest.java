package brig.concord.documentation;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.dependency.TaskRegistry;
import brig.concord.meta.model.TaskStepMetaType.TaskNameLookup;
import brig.concord.schema.TaskSchemaRegistry;
import brig.concord.yaml.meta.model.TypeFieldPair;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.platform.backend.documentation.DocumentationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LookupDocumentationTest extends ConcordYamlTestBaseJunit5 {

    private final ConcordLookupDocumentationProvider provider = new ConcordLookupDocumentationProvider();

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        var registry = TaskSchemaRegistry.getInstance(getProject());
        registry.setProvider(taskName -> {
            var path = "/taskSchema/" + taskName + ".schema.json";
            return LookupDocumentationTest.class.getResourceAsStream(path);
        });
    }

    @Test
    void testRootKeyDocumentation() {
        configureFromText("""
                <caret>
                """);

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups);
        assertTrue(lookups.length > 0);

        var foundDocumented = false;
        for (var lookup : lookups) {
            if (!(lookup.getObject() instanceof TypeFieldPair)) {
                continue;
            }

            var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
            var name = lookup.getLookupString();

            if ("flows".equals(name) || "configuration".equals(name)) {
                assertNotNull(target, "Expected documentation target for key: " + name);
                assertNotNull(target.computeDocumentationHint(), "Expected hint for key: " + name);

                var doc = target.computeDocumentation();
                assertNotNull(doc, "Expected documentation for key: " + name);
                assertInstanceOf(DocumentationData.class, doc);

                var html = ((DocumentationData) doc).getHtml();
                assertNotNull(html);
                assertTrue(html.contains(name), "Documentation should contain key name: " + name);

                foundDocumented = true;
            }
        }
        assertTrue(foundDocumented, "Should find at least one documented root key");
    }

    @Test
    void testStepKeyDocumentation() {
        configureFromText("""
                flows:
                  main:
                    - <caret>
                """);

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups);

        var foundTask = false;
        for (var lookup : lookups) {
            if (!(lookup.getObject() instanceof TypeFieldPair)) {
                continue;
            }

            var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
            var name = lookup.getLookupString();

            if ("task".equals(name)) {
                assertNotNull(target, "Expected documentation target for step: task");
                assertNotNull(target.computeDocumentationHint());

                var doc = target.computeDocumentation();
                assertNotNull(doc);
                assertInstanceOf(DocumentationData.class, doc);

                var html = ((DocumentationData) doc).getHtml();
                assertTrue(html.contains("task"));

                foundTask = true;
            }
        }
        assertTrue(foundTask, "Should find documented 'task' step key");
    }

    @Test
    void testTaskNameDocumentation() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                """);

        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("strictTask"));

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups);

        var foundDoc = false;
        for (var lookup : lookups) {
            if (!(lookup.getObject() instanceof TaskNameLookup)) {
                continue;
            }

            assertEquals("strictTask", lookup.getLookupString());

            var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
            assertNotNull(target, "Expected documentation target for task name: strictTask");

            var doc = target.computeDocumentation();
            assertNotNull(doc);
            assertInstanceOf(DocumentationData.class, doc);

            var html = ((DocumentationData) doc).getHtml();
            // Should contain the task name and parameter info
            assertTrue(html.contains("strictTask"), "Documentation should contain task name");
            assertTrue(html.contains("url"), "Documentation should contain 'url' parameter");
            assertTrue(html.contains("method"), "Documentation should contain 'method' parameter");

            foundDoc = true;
        }
        assertTrue(foundDoc, "Should find documentation for task name lookup");
    }

    @Test
    void testTaskNameWithNoSchema() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                """);

        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("unknownTask"));

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups);

        for (var lookup : lookups) {
            if (lookup.getObject() instanceof TaskNameLookup) {
                var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
                assertNull(target, "Should return null for task without schema");
            }
        }
    }

    @Test
    void testTaskParamKeyDocumentation() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      in:
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups);

        var foundUrl = false;
        for (var lookup : lookups) {
            if (!(lookup.getObject() instanceof TypeFieldPair)) {
                continue;
            }

            var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
            var name = lookup.getLookupString();

            if ("url".equals(name)) {
                assertNotNull(target, "Expected documentation target for param: url");
                assertEquals("URL to fetch", target.computeDocumentationHint());

                var doc = target.computeDocumentation();
                assertNotNull(doc);
                assertInstanceOf(DocumentationData.class, doc);

                var html = ((DocumentationData) doc).getHtml();
                assertTrue(html.contains("url"), "Documentation should contain param name");
                assertTrue(html.contains("URL to fetch"), "Documentation should contain description");

                foundUrl = true;
            }
        }
        assertTrue(foundUrl, "Should find documentation for 'url' task parameter");
    }

    @Test
    void testDocumentationTargetPointerRoundTrip() {
        configureFromText("""
                <caret>
                """);

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups);

        var verified = 0;
        for (var lookup : lookups) {
            if (!(lookup.getObject() instanceof TypeFieldPair)) {
                continue;
            }

            var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
            if (target == null) {
                continue;
            }

            var pointer = target.createPointer();
            var restored = pointer.dereference();
            assertNotNull(restored, "Pointer should dereference for key: " + lookup.getLookupString());
            assertEquals(target.computeDocumentationHint(), restored.computeDocumentationHint());
            verified++;
        }
        assertTrue(verified > 0, "Should verify at least one pointer round-trip");
    }

    @Test
    void testTaskNameDocumentationPointerRoundTrip() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                """);

        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("strictTask"));

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups);

        var verified = false;
        for (var lookup : lookups) {
            if (!(lookup.getObject() instanceof TaskNameLookup)) {
                continue;
            }

            var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
            if (target == null) {
                continue;
            }

            var pointer = target.createPointer();
            var restored = pointer.dereference();
            assertNotNull(restored, "Pointer should dereference for task name: " + lookup.getLookupString());
            assertEquals(target.computeDocumentationHint(), restored.computeDocumentationHint());
            verified = true;
        }
        assertTrue(verified, "Should verify pointer round-trip for TaskNameLookup");
    }

    @Test
    void testNonConcordFileReturnsNull() {
        var file = myFixture.configureByText("test.txt", "hello");

        myFixture.complete(CompletionType.BASIC);
        var lookups = myFixture.getLookupElements();
        if (lookups != null) {
            for (var lookup : lookups) {
                var target = provider.documentationTarget(file, lookup, 0);
                assertNull(target, "Should return null for non-Concord files");
            }
        }
    }
}
