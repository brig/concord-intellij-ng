// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskSchemaSectionTest {

    @Test
    void testEmptySection() {
        var empty = TaskSchemaSection.empty();
        assertTrue(empty.properties().isEmpty());
        assertTrue(empty.requiredFields().isEmpty());
        assertTrue(empty.additionalProperties());
    }

    @Test
    void testMergeUnionOfProperties() {
        var a = section(
                Map.of("url", prop("url", "string")),
                Set.of(),
                true
        );
        var b = section(
                Map.of("method", prop("method", "string")),
                Set.of(),
                true
        );

        var merged = a.merge(b);
        assertEquals(2, merged.properties().size());
        assertTrue(merged.properties().containsKey("url"));
        assertTrue(merged.properties().containsKey("method"));
    }

    @Test
    void testMergeUnionOfRequiredFields() {
        var a = section(
                Map.of("url", prop("url", "string")),
                Set.of("url"),
                true
        );
        var b = section(
                Map.of("method", prop("method", "string")),
                Set.of("method"),
                true
        );

        var merged = a.merge(b);
        assertTrue(merged.requiredFields().contains("url"));
        assertTrue(merged.requiredFields().contains("method"));
    }

    @Test
    void testMergeAdditionalPropertiesAndSemantics() {
        var trueAndFalse = section(Map.of(), Set.of(), true)
                .merge(section(Map.of(), Set.of(), false));
        assertFalse(trueAndFalse.additionalProperties());

        var falseAndTrue = section(Map.of(), Set.of(), false)
                .merge(section(Map.of(), Set.of(), true));
        assertFalse(falseAndTrue.additionalProperties());

        var trueAndTrue = section(Map.of(), Set.of(), true)
                .merge(section(Map.of(), Set.of(), true));
        assertTrue(trueAndTrue.additionalProperties());

        var falseAndFalse = section(Map.of(), Set.of(), false)
                .merge(section(Map.of(), Set.of(), false));
        assertFalse(falseAndFalse.additionalProperties());
    }

    @Test
    void testMergeOverlappingPropertySecondWins() {
        var a = section(
                Map.of("action", prop("action", "string")),
                Set.of(),
                true
        );
        var b = section(
                Map.of("action", prop("action", "boolean")),
                Set.of(),
                true
        );

        var merged = a.merge(b);
        assertEquals(1, merged.properties().size());
        assertEquals(new SchemaType.Scalar("boolean"), merged.properties().get("action").schemaType());
    }

    @Test
    void testMergeRequiredFlagPropagatedToExistingProperty() {
        // Property exists in first section as not-required.
        // Second section's requiredFields marks it required.
        var a = section(
                Map.of("url", prop("url", "string")),
                Set.of(),
                true
        );
        var b = section(
                Map.of(),
                Set.of("url"),
                true
        );

        var merged = a.merge(b);
        assertTrue(merged.properties().get("url").required());
        assertTrue(merged.requiredFields().contains("url"));
    }

    @Test
    void testMergeWithEmptyIsIdentity() {
        var original = section(
                Map.of("url", prop("url", "string"), "method", prop("method", "string")),
                Set.of("url"),
                false
        );

        var mergedRight = original.merge(TaskSchemaSection.empty());
        assertEquals(original.properties().size(), mergedRight.properties().size());
        assertEquals(original.requiredFields(), mergedRight.requiredFields());
        // empty has additionalProperties=true, AND semantics: false && true = false
        assertFalse(mergedRight.additionalProperties());

        var mergedLeft = TaskSchemaSection.empty().merge(original);
        assertEquals(original.properties().size(), mergedLeft.properties().size());
        assertEquals(original.requiredFields(), mergedLeft.requiredFields());
        assertFalse(mergedLeft.additionalProperties());
    }

    @Test
    void testMergePropertyFromFirstBecomeRequiredViaSecondRequiredFields() {
        var a = section(
                Map.of("timeout", prop("timeout", "integer")),
                Set.of(),
                true
        );
        var b = section(
                Map.of("retries", prop("retries", "integer")),
                Set.of("timeout"),
                true
        );

        var merged = a.merge(b);
        // timeout comes from first section, required comes from second section's requiredFields
        assertTrue(merged.properties().get("timeout").required());
        assertFalse(merged.properties().get("retries").required());
        assertTrue(merged.requiredFields().contains("timeout"));
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
