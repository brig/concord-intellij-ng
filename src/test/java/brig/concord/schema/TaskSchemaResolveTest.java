// SPDX-License-Identifier: Apache-2.0
package brig.concord.schema;

import brig.concord.ConcordType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskSchemaResolveTest {

    @Test
    void testNoConditionals() {
        var base = section(
                Map.of("url", prop("url", ConcordType.WellKnown.STRING)),
                Set.of("url"),
                true
        );
        var schema = new TaskSchema("test", null, base, List.of(), ObjectSchema.empty());

        var resolved = schema.resolveInSection(Map.of("url", "http://example.com"));
        assertEquals(base.properties().size(), resolved.properties().size());
        assertTrue(resolved.properties().containsKey("url"));
    }

    @Test
    void testSingleConditionalMatch() {
        var base = section(
                Map.of("action", prop("action", ConcordType.WellKnown.STRING)),
                Set.of("action"),
                true
        );
        var thenSection = section(
                Map.of("project", prop("project", ConcordType.WellKnown.STRING)),
                Set.of("project"),
                true
        );
        var conditional = new SchemaConditional(
                Map.of("action", List.of("start")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), ObjectSchema.empty());

        var resolved = schema.resolveInSection(Map.of("action", "start"));
        assertTrue(resolved.properties().containsKey("action"));
        assertTrue(resolved.properties().containsKey("project"));
        assertTrue(resolved.requiredFields().contains("project"));
    }

    @Test
    void testSingleConditionalNoMatch() {
        var base = section(
                Map.of("action", prop("action", ConcordType.WellKnown.STRING)),
                Set.of("action"),
                true
        );
        var thenSection = section(
                Map.of("project", prop("project", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var conditional = new SchemaConditional(
                Map.of("action", List.of("start")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), ObjectSchema.empty());

        var resolved = schema.resolveInSection(Map.of("action", "kill"));
        assertTrue(resolved.properties().containsKey("action"));
        assertFalse(resolved.properties().containsKey("project"));
    }

    @Test
    void testMultipleConditionalsOneMatches() {
        var base = section(
                Map.of("action", prop("action", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var startThen = section(
                Map.of("project", prop("project", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var killThen = section(
                Map.of("instanceId", prop("instanceId", ConcordType.WellKnown.STRING)),
                Set.of("instanceId"),
                true
        );
        var schema = new TaskSchema("test", null, base, List.of(
                new SchemaConditional(Map.of("action", List.of("start")), startThen),
                new SchemaConditional(Map.of("action", List.of("kill")), killThen)
        ), ObjectSchema.empty());

        var resolved = schema.resolveInSection(Map.of("action", "kill"));
        assertTrue(resolved.properties().containsKey("action"));
        assertFalse(resolved.properties().containsKey("project"));
        assertTrue(resolved.properties().containsKey("instanceId"));
        assertTrue(resolved.requiredFields().contains("instanceId"));
    }

    @Test
    void testMultipleConditionalsBothMatch() {
        var base = section(
                Map.of("action", prop("action", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        // Both conditionals match "start"
        var first = section(
                Map.of("project", prop("project", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var second = section(
                Map.of("entryPoint", prop("entryPoint", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var schema = new TaskSchema("test", null, base, List.of(
                new SchemaConditional(Map.of("action", List.of("start")), first),
                new SchemaConditional(Map.of("action", List.of("start")), second)
        ), ObjectSchema.empty());

        var resolved = schema.resolveInSection(Map.of("action", "start"));
        assertTrue(resolved.properties().containsKey("action"));
        assertTrue(resolved.properties().containsKey("project"));
        assertTrue(resolved.properties().containsKey("entryPoint"));
    }

    @Test
    void testMultiKeyDiscriminatorAllMatch() {
        var base = section(
                Map.of(
                        "action", prop("action", ConcordType.WellKnown.STRING),
                        "mode", prop("mode", ConcordType.WellKnown.STRING)
                ),
                Set.of(),
                true
        );
        var thenSection = section(
                Map.of("timeout", prop("timeout", ConcordType.WellKnown.INTEGER)),
                Set.of(),
                true
        );
        var conditional = new SchemaConditional(
                Map.of("action", List.of("process"), "mode", List.of("sync")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), ObjectSchema.empty());

        var resolved = schema.resolveInSection(Map.of("action", "process", "mode", "sync"));
        assertTrue(resolved.properties().containsKey("timeout"));
    }

    @Test
    void testMultiKeyDiscriminatorPartialMatch() {
        var base = section(
                Map.of(
                        "action", prop("action", ConcordType.WellKnown.STRING),
                        "mode", prop("mode", ConcordType.WellKnown.STRING)
                ),
                Set.of(),
                true
        );
        var thenSection = section(
                Map.of("timeout", prop("timeout", ConcordType.WellKnown.INTEGER)),
                Set.of(),
                true
        );
        var conditional = new SchemaConditional(
                Map.of("action", List.of("process"), "mode", List.of("sync")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), ObjectSchema.empty());

        // Only action matches, mode is missing -> no match
        var resolved = schema.resolveInSection(Map.of("action", "process"));
        assertFalse(resolved.properties().containsKey("timeout"));
    }

    @Test
    void testEnumStyleDiscriminator() {
        var base = section(
                Map.of("action", prop("action", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var thenSection = section(
                Map.of("target", prop("target", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        // Discriminator accepts multiple values (enum-style)
        var conditional = new SchemaConditional(
                Map.of("action", List.of("create", "update", "upsert")),
                thenSection
        );
        var schema = new TaskSchema("test", null, base, List.of(conditional), ObjectSchema.empty());

        assertTrue(schema.resolveInSection(Map.of("action", "create")).properties().containsKey("target"));
        assertTrue(schema.resolveInSection(Map.of("action", "update")).properties().containsKey("target"));
        assertTrue(schema.resolveInSection(Map.of("action", "upsert")).properties().containsKey("target"));
        assertFalse(schema.resolveInSection(Map.of("action", "delete")).properties().containsKey("target"));
    }

    @Test
    void testGetDiscriminatorKeysDeduped() {
        var base = ObjectSchema.empty();
        var schema = new TaskSchema("test", null, base, List.of(
                new SchemaConditional(Map.of("action", List.of("a"), "mode", List.of("x")), ObjectSchema.empty()),
                new SchemaConditional(Map.of("action", List.of("b"), "level", List.of("high")), ObjectSchema.empty()),
                new SchemaConditional(Map.of("mode", List.of("y")), ObjectSchema.empty())
        ), ObjectSchema.empty());

        var keys = schema.getDiscriminatorKeys();
        // "action" appears in two conditionals, "mode" in two – should be deduped
        assertEquals(Set.of("action", "mode", "level"), keys);
        assertEquals(3, keys.size());
    }

    // -- helpers --

    private static SchemaProperty prop(String name, ConcordType type) {
        return new SchemaProperty(name, new SchemaType.Scalar(type), null, false);
    }

    private static ObjectSchema section(Map<String, SchemaProperty> props,
                                             Set<String> required,
                                             boolean additionalProperties) {
        return new ObjectSchema(
                new LinkedHashMap<>(props),
                new LinkedHashSet<>(required),
                additionalProperties
        );
    }
}
