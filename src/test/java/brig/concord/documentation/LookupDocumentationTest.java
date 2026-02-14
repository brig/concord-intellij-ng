package brig.concord.documentation;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.dependency.TaskRegistry;
import brig.concord.meta.model.TaskStepMetaType.TaskNameLookup;
import brig.concord.schema.TaskSchemaRegistry;
import brig.concord.yaml.meta.model.TypeFieldPair;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.platform.backend.documentation.DocumentationData;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import org.jetbrains.annotations.NotNull;
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

        var target = findLookupTarget("flows", TypeFieldPair.class);
        assertLookupDoc(target, "/documentation/lookup/root-flows.html");
    }

    @Test
    void testStepKeyDocumentation() {
        configureFromText("""
                flows:
                  main:
                    - <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var target = findLookupTarget("task", TypeFieldPair.class);
        assertLookupDoc(target, "/documentation/lookup/step-task.html");
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

        var target = findLookupTarget("strictTask", TaskNameLookup.class);
        assertLookupDoc(target, "/documentation/lookup/taskname-strictTask.html");
    }

    @Test
    void testConcordTaskNameDocumentation() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                """);

        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("concord"));

        myFixture.complete(CompletionType.BASIC);

        var target = findLookupTarget("concord", TaskNameLookup.class);
        assertLookupDoc(target, "/documentation/lookup/taskname-concord.html");
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

        var target = findLookupTarget("url", TypeFieldPair.class);
        assertLookupDoc(target, "/documentation/lookup/taskparam-url.html");
    }

    @Test
    void testTaskParamEnumDocumentation() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var target = findLookupTarget("action", TypeFieldPair.class);
        assertLookupDoc(target, "/documentation/lookup/taskparam-action.html");
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

    private @NotNull DocumentationTarget findLookupTarget(String name, Class<?> objectType) {
        var lookups = myFixture.getLookupElements();
        assertNotNull(lookups, "Completion should produce lookup elements");

        for (var lookup : lookups) {
            if (!objectType.isInstance(lookup.getObject())) {
                continue;
            }
            if (!name.equals(lookup.getLookupString())) {
                continue;
            }

            var target = provider.documentationTarget(myFixture.getFile(), lookup, 0);
            assertNotNull(target, "Expected documentation target for: " + name);
            return target;
        }

        fail("Lookup element '" + name + "' of type " + objectType.getSimpleName() + " not found in completion list");
        return null; // unreachable
    }

    private void assertLookupDoc(DocumentationTarget target, String htmlResource) {
        var doc = target.computeDocumentation();
        assertNotNull(doc, "Expected documentation");
        assertInstanceOf(DocumentationData.class, doc);

        var actualHtml = ((DocumentationData) doc).getHtml();
        var expectedHtml = loadResource(htmlResource);

        assertEquals(
                expectedHtml.replaceAll("\\s+", "").trim(),
                actualHtml.replaceAll("\\s+", "").trim(),
                actualHtml
        );
    }
}
