package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlStringType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IdentityElementMetaTypeTest {

    private static class TestEntry extends IdentityMetaType {

        private final Map<String, YamlMetaType> features;

        TestEntry(String identity, String... featureNames) {
            super(identity, Set.of());

            var map = new LinkedHashMap<String, YamlMetaType>();
            map.put(identity, YamlStringType.getInstance());
            for (var f : featureNames) {
                map.put(f, YamlStringType.getInstance());
            }
            this.features = Collections.unmodifiableMap(map);
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }
    }

    private static class TestContainer extends IdentityElementMetaType {

        TestContainer(IdentityMetaType... entries) {
            super("test-container", List.of(entries));
        }

        @Override
        public IdentityMetaType identifyEntry(Set<String> existingKeys) {
            return super.identifyEntry(existingKeys);
        }

        @Override
        public IdentityMetaType guessEntry(Set<String> existingKeys) {
            return super.guessEntry(existingKeys);
        }

        @Override
        public boolean hasFeature(String name) {
            return super.hasFeature(name);
        }
    }

    // entries that mimic a realistic scenario:
    //   taskEntry:  identity="task",  features=[task, in, out, meta, error, retry]
    //   exprEntry:  identity="expr",  features=[expr, out, meta, error]
    //   callEntry:  identity="call",  features=[call, in, out, retry, loop]
    private final TestEntry taskEntry = new TestEntry("task", "in", "out", "meta", "error", "retry");
    private final TestEntry exprEntry = new TestEntry("expr", "out", "meta", "error");
    private final TestEntry callEntry = new TestEntry("call", "in", "out", "retry", "loop");

    private final TestContainer container = new TestContainer(taskEntry, exprEntry, callEntry);

    // -- identifyEntry tests --

    @Test
    void identifyEntry_exactMatchByIdentityKey() {
        assertEquals(taskEntry, container.identifyEntry(Set.of("task", "in", "out")));
        assertEquals(exprEntry, container.identifyEntry(Set.of("expr")));
        assertEquals(callEntry, container.identifyEntry(Set.of("call", "loop")));
    }

    @Test
    void identifyEntry_returnsNullWhenNoIdentityKey() {
        assertNull(container.identifyEntry(Set.of("in", "out")));
        assertNull(container.identifyEntry(Set.of("meta")));
        assertNull(container.identifyEntry(Set.of()));
    }

    @Test
    void identifyEntry_firstMatchWinsWhenMultipleIdentitiesPresent() {
        // if both "task" and "expr" are in the keys, the first declared entry wins
        assertEquals(taskEntry, container.identifyEntry(Set.of("task", "expr")));
    }

    // -- guessEntry tests --

    @Test
    void guessEntry_bestMatchByFeatureOverlap() {
        // "in" + "retry" overlap with task (2 features) and call (2 features),
        // but task is declared first, so task wins
        assertEquals(taskEntry, container.guessEntry(Set.of("in", "retry")));

        // "loop" only matches call
        assertEquals(callEntry, container.guessEntry(Set.of("loop")));

        // "out" + "meta" + "error" matches task(3) and expr(3) — task first
        assertEquals(taskEntry, container.guessEntry(Set.of("out", "meta", "error")));
    }

    @Test
    void guessEntry_uniqueFeatureMatchesCorrectEntry() {
        // "loop" is unique to callEntry
        assertEquals(callEntry, container.guessEntry(Set.of("loop")));

        // "in" + "loop" is 2 matches for callEntry, only 1 (in) for taskEntry
        assertEquals(callEntry, container.guessEntry(Set.of("in", "loop")));
    }

    @Test
    void guessEntry_returnsNullWhenNoFeaturesMatch() {
        assertNull(container.guessEntry(Set.of("unknown")));
        assertNull(container.guessEntry(Set.of()));
    }

    @Test
    void guessEntry_tieBreakingFirstEntryWins() {
        // "out" alone matches task, expr, and call — all with 1 match.
        // task is first in declaration order.
        assertEquals(taskEntry, container.guessEntry(Set.of("out")));
    }

    // -- hasFeature tests --

    @Test
    void hasFeature_trueForKnownFeatures() {
        assertTrue(container.hasFeature("task"));
        assertTrue(container.hasFeature("in"));
        assertTrue(container.hasFeature("out"));
        assertTrue(container.hasFeature("meta"));
        assertTrue(container.hasFeature("error"));
        assertTrue(container.hasFeature("retry"));
        assertTrue(container.hasFeature("expr"));
        assertTrue(container.hasFeature("call"));
        assertTrue(container.hasFeature("loop"));
    }

    @Test
    void hasFeature_falseForUnknownFeature() {
        assertFalse(container.hasFeature("unknown"));
        assertFalse(container.hasFeature(""));
    }

    // -- findFeatureByName integration --

    @Test
    void findFeatureByName_returnsFieldForKnownFeature() {
        assertNotNull(container.findFeatureByName("task"));
        assertNotNull(container.findFeatureByName("in"));
        assertNotNull(container.findFeatureByName("loop"));
    }

    @Test
    void findFeatureByName_returnsNullForUnknownFeature() {
        assertNull(container.findFeatureByName("unknown"));
    }

    // -- resolve (DynamicMetaType) tests --

    @Test
    void resolve_returnsNullForNonMappingElement() {
        assertNull(container.resolve(null));
    }
}
