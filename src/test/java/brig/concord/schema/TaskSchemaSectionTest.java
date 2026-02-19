package brig.concord.schema;

import brig.concord.ConcordType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ObjectSchemaTest {

    @Test
    void testEmptySection() {
        var empty = ObjectSchema.empty();
        assertTrue(empty.properties().isEmpty());
        assertTrue(empty.requiredFields().isEmpty());
        assertTrue(empty.additionalProperties());
    }

    @Test
    void testMergeUnionOfProperties() {
        var a = section(
                Map.of("url", prop("url", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var b = section(
                Map.of("method", prop("method", ConcordType.WellKnown.STRING)),
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
                Map.of("url", prop("url", ConcordType.WellKnown.STRING)),
                Set.of("url"),
                true
        );
        var b = section(
                Map.of("method", prop("method", ConcordType.WellKnown.STRING)),
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
                Map.of("action", prop("action", ConcordType.WellKnown.STRING)),
                Set.of(),
                true
        );
        var b = section(
                Map.of("action", prop("action", ConcordType.WellKnown.BOOLEAN)),
                Set.of(),
                true
        );

        var merged = a.merge(b);
        assertEquals(1, merged.properties().size());
        assertEquals(SchemaType.Scalar.BOOLEAN, merged.properties().get("action").schemaType());
    }

    @Test
    void testMergeRequiredFlagPropagatedToExistingProperty() {
        // Property exists in first section as not-required.
        // Second section's requiredFields marks it required.
        var a = section(
                Map.of("url", prop("url", ConcordType.WellKnown.STRING)),
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
                Map.of("url", prop("url", ConcordType.WellKnown.STRING), "method", prop("method", ConcordType.WellKnown.STRING)),
                Set.of("url"),
                false
        );

        var mergedRight = original.merge(ObjectSchema.empty());
        assertEquals(original.properties().size(), mergedRight.properties().size());
        assertEquals(original.requiredFields(), mergedRight.requiredFields());
        // empty has additionalProperties=true, AND semantics: false && true = false
        assertFalse(mergedRight.additionalProperties());

        var mergedLeft = ObjectSchema.empty().merge(original);
        assertEquals(original.properties().size(), mergedLeft.properties().size());
        assertEquals(original.requiredFields(), mergedLeft.requiredFields());
        assertFalse(mergedLeft.additionalProperties());
    }

    @Test
    void testMergePropertyFromFirstBecomeRequiredViaSecondRequiredFields() {
        var a = section(
                Map.of("timeout", prop("timeout", ConcordType.WellKnown.INTEGER)),
                Set.of(),
                true
        );
        var b = section(
                Map.of("retries", prop("retries", ConcordType.WellKnown.INTEGER)),
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
