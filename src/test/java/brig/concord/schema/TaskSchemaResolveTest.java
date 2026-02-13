package brig.concord.schema;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskSchemaResolveTest {

    @Test
    void testNoConditionals() {
        var base = section(
                Map.of("url", prop("url", "string")),
                Set.of("url"),
                true
        );
        var schema = new TaskSchema("test", null, base, List.of(), TaskSchemaSection.empty());

        var resolved = schema.resolveInSection(Map.of("url", "http://example.com"));
        assertEquals(base.properties().size(), resolved.properties().size());
        assertTrue(resolved.properties().containsKey("url"));
    }

    @Test
    void testSingleConditionalMatch() {
        var base = section(
                Map.of("action", prop("action", "string")),
                Set.of("action"),
                true
        );
        var thenSection = section(
                Map.of("project", prop("project", "string")),
                Set.of("project"),
                true
        );
        var conditional = new TaskSchemaConditional(
                Map.of("action", List.of("start")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), TaskSchemaSection.empty());

        var resolved = schema.resolveInSection(Map.of("action", "start"));
        assertTrue(resolved.properties().containsKey("action"));
        assertTrue(resolved.properties().containsKey("project"));
        assertTrue(resolved.requiredFields().contains("project"));
    }

    @Test
    void testSingleConditionalNoMatch() {
        var base = section(
                Map.of("action", prop("action", "string")),
                Set.of("action"),
                true
        );
        var thenSection = section(
                Map.of("project", prop("project", "string")),
                Set.of(),
                true
        );
        var conditional = new TaskSchemaConditional(
                Map.of("action", List.of("start")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), TaskSchemaSection.empty());

        var resolved = schema.resolveInSection(Map.of("action", "kill"));
        assertTrue(resolved.properties().containsKey("action"));
        assertFalse(resolved.properties().containsKey("project"));
    }

    @Test
    void testMultipleConditionalsOneMatches() {
        var base = section(
                Map.of("action", prop("action", "string")),
                Set.of(),
                true
        );
        var startThen = section(
                Map.of("project", prop("project", "string")),
                Set.of(),
                true
        );
        var killThen = section(
                Map.of("instanceId", prop("instanceId", "string")),
                Set.of("instanceId"),
                true
        );
        var schema = new TaskSchema("test", null, base, List.of(
                new TaskSchemaConditional(Map.of("action", List.of("start")), startThen),
                new TaskSchemaConditional(Map.of("action", List.of("kill")), killThen)
        ), TaskSchemaSection.empty());

        var resolved = schema.resolveInSection(Map.of("action", "kill"));
        assertTrue(resolved.properties().containsKey("action"));
        assertFalse(resolved.properties().containsKey("project"));
        assertTrue(resolved.properties().containsKey("instanceId"));
        assertTrue(resolved.requiredFields().contains("instanceId"));
    }

    @Test
    void testMultipleConditionalsBothMatch() {
        var base = section(
                Map.of("action", prop("action", "string")),
                Set.of(),
                true
        );
        // Both conditionals match "start"
        var first = section(
                Map.of("project", prop("project", "string")),
                Set.of(),
                true
        );
        var second = section(
                Map.of("entryPoint", prop("entryPoint", "string")),
                Set.of(),
                true
        );
        var schema = new TaskSchema("test", null, base, List.of(
                new TaskSchemaConditional(Map.of("action", List.of("start")), first),
                new TaskSchemaConditional(Map.of("action", List.of("start")), second)
        ), TaskSchemaSection.empty());

        var resolved = schema.resolveInSection(Map.of("action", "start"));
        assertTrue(resolved.properties().containsKey("action"));
        assertTrue(resolved.properties().containsKey("project"));
        assertTrue(resolved.properties().containsKey("entryPoint"));
    }

    @Test
    void testMultiKeyDiscriminatorAllMatch() {
        var base = section(
                Map.of(
                        "action", prop("action", "string"),
                        "mode", prop("mode", "string")
                ),
                Set.of(),
                true
        );
        var thenSection = section(
                Map.of("timeout", prop("timeout", "integer")),
                Set.of(),
                true
        );
        var conditional = new TaskSchemaConditional(
                Map.of("action", List.of("process"), "mode", List.of("sync")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), TaskSchemaSection.empty());

        var resolved = schema.resolveInSection(Map.of("action", "process", "mode", "sync"));
        assertTrue(resolved.properties().containsKey("timeout"));
    }

    @Test
    void testMultiKeyDiscriminatorPartialMatch() {
        var base = section(
                Map.of(
                        "action", prop("action", "string"),
                        "mode", prop("mode", "string")
                ),
                Set.of(),
                true
        );
        var thenSection = section(
                Map.of("timeout", prop("timeout", "integer")),
                Set.of(),
                true
        );
        var conditional = new TaskSchemaConditional(
                Map.of("action", List.of("process"), "mode", List.of("sync")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), TaskSchemaSection.empty());

        // Only action matches, mode is missing → no match
        var resolved = schema.resolveInSection(Map.of("action", "process"));
        assertFalse(resolved.properties().containsKey("timeout"));
    }

    @Test
    void testEnumStyleDiscriminator() {
        var base = section(
                Map.of("action", prop("action", "string")),
                Set.of(),
                true
        );
        var thenSection = section(
                Map.of("target", prop("target", "string")),
                Set.of(),
                true
        );
        // Discriminator accepts multiple values (enum-style)
        var conditional = new TaskSchemaConditional(
                Map.of("action", List.of("create", "update", "upsert")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), TaskSchemaSection.empty());

        assertTrue(schema.resolveInSection(Map.of("action", "create")).properties().containsKey("target"));
        assertTrue(schema.resolveInSection(Map.of("action", "update")).properties().containsKey("target"));
        assertTrue(schema.resolveInSection(Map.of("action", "upsert")).properties().containsKey("target"));
        assertFalse(schema.resolveInSection(Map.of("action", "delete")).properties().containsKey("target"));
    }

    @Test
    void testGetDiscriminatorKeysDeduped() {
        var base = TaskSchemaSection.empty();
        var schema = new TaskSchema("test", null, base, List.of(
                new TaskSchemaConditional(Map.of("action", List.of("a"), "mode", List.of("x")), TaskSchemaSection.empty()),
                new TaskSchemaConditional(Map.of("action", List.of("b"), "level", List.of("high")), TaskSchemaSection.empty()),
                new TaskSchemaConditional(Map.of("mode", List.of("y")), TaskSchemaSection.empty())
        ), TaskSchemaSection.empty());

        var keys = schema.getDiscriminatorKeys();
        // "action" appears in two conditionals, "mode" in two – should be deduped
        assertEquals(Set.of("action", "mode", "level"), keys);
        assertEquals(3, keys.size());
    }

    // -- helpers --

    private static TaskSchemaProperty prop(String name, String type) {
        return new TaskSchemaProperty(name, new SchemaType.Scalar(type), null, false);
    }

    private static TaskSchemaSection section(Map<String, TaskSchemaProperty> props,
                                             Set<String> required,
                                             boolean additionalProperties) {
        return new TaskSchemaSection(
                new LinkedHashMap<>(props),
                new LinkedHashSet<>(required),
                additionalProperties
        );
    }
}
