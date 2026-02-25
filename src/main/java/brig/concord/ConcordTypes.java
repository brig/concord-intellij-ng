// SPDX-License-Identifier: Apache-2.0
package brig.concord;

import brig.concord.ConcordType.WellKnown;
import brig.concord.ConcordType.YamlBaseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ConcordTypes {

    private static final Map<String, ConcordType> ALIASES = Map.ofEntries(
            Map.entry("string", WellKnown.STRING),
            Map.entry("boolean", WellKnown.BOOLEAN),
            Map.entry("int", WellKnown.INTEGER),
            Map.entry("integer", WellKnown.INTEGER),
            Map.entry("number", WellKnown.INTEGER),
            Map.entry("object", WellKnown.OBJECT),
            Map.entry("regexp", WellKnown.REGEXP),
            Map.entry("any", WellKnown.ANY)
    );

    public static @NotNull Map<String, ConcordType> aliases() {
        return ALIASES;
    }

    /**
     * Resolves a type name to a known ConcordType.
     * Handles aliases case-insensitively (e.g., "int" -> INTEGER, "number" -> INTEGER).
     *
     * @return the matching ConcordType, or null if the type name is not recognized
     */
    public static @Nullable ConcordType fromString(@NotNull String name) {
        return ALIASES.get(name.toLowerCase());
    }

    /**
     * Resolves a type name to a ConcordType, creating a {@link ConcordType.Custom} instance
     * for unrecognized types. The {@code fallbackBase} determines the structural
     * YAML type used for validation and meta type mapping of unknown types.
     *
     * @param name         the type name to resolve
     * @param fallbackBase the YAML base type to use if the name is not recognized
     * @return the matching known ConcordType, or a Custom instance for unknown types
     */
    public static @NotNull ConcordType resolve(@NotNull String name,
                                               @NotNull YamlBaseType fallbackBase) {
        var known = fromString(name);
        if (known != null) {
            return known;
        }
        return new ConcordType.Custom(name, fallbackBase);
    }

    private ConcordTypes() {}
}
