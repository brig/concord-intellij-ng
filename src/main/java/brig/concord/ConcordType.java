package brig.concord;

import brig.concord.meta.model.value.AnythingMetaType;
import brig.concord.meta.model.value.AnyOfType;
import brig.concord.meta.model.value.ParamMetaTypes;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public sealed interface ConcordType {

    @NotNull String displayName();

    @NotNull YamlBaseType yamlBaseType();

    default @NotNull YamlMetaType scalarMetaType(@Nullable TypeProps props) {
        return yamlBaseType().scalarMetaType(props);
    }

    default @NotNull AnyOfType arrayMetaType(@Nullable TypeProps props) {
        return yamlBaseType().arrayMetaType(props);
    }

    enum YamlBaseType {
        STRING,
        BOOLEAN,
        INTEGER,
        OBJECT,
        ANY;

        public @NotNull YamlMetaType scalarMetaType(@Nullable TypeProps props) {
            AnyOfType base = switch (this) {
                case STRING -> ParamMetaTypes.STRING_OR_EXPRESSION;
                case BOOLEAN -> ParamMetaTypes.BOOLEAN_OR_EXPRESSION;
                case INTEGER -> ParamMetaTypes.NUMBER_OR_EXPRESSION;
                case OBJECT -> ParamMetaTypes.OBJECT_OR_EXPRESSION;
                case ANY -> null;
            };
            if (base == null) {
                return props != null ? new AnythingMetaType(props) : AnythingMetaType.getInstance();
            }
            return props != null ? base.withProps(props) : base;
        }

        public @NotNull AnyOfType arrayMetaType(@Nullable TypeProps props) {
            AnyOfType base = switch (this) {
                case STRING -> ParamMetaTypes.STRING_ARRAY_OR_EXPRESSION;
                case BOOLEAN -> ParamMetaTypes.BOOLEAN_ARRAY_OR_EXPRESSION;
                case INTEGER -> ParamMetaTypes.NUMBER_ARRAY_OR_EXPRESSION;
                case OBJECT -> ParamMetaTypes.OBJECT_ARRAY_OR_EXPRESSION;
                case ANY -> ParamMetaTypes.ARRAY_OR_EXPRESSION;
            };
            return props != null ? base.withProps(props) : base;
        }
    }

    enum WellKnown implements ConcordType {
        STRING("string", YamlBaseType.STRING),
        BOOLEAN("boolean", YamlBaseType.BOOLEAN),
        INTEGER("integer", YamlBaseType.INTEGER),
        OBJECT("object", YamlBaseType.OBJECT),
        ANY("any", YamlBaseType.ANY),
        REGEXP("regexp", YamlBaseType.STRING) {
            @Override
            public @NotNull YamlMetaType scalarMetaType(@Nullable TypeProps props) {
                return props != null
                        ? ParamMetaTypes.REGEXP_OR_EXPRESSION.withProps(props)
                        : ParamMetaTypes.REGEXP_OR_EXPRESSION;
            }

            @Override
            public @NotNull AnyOfType arrayMetaType(@Nullable TypeProps props) {
                return props != null
                        ? ParamMetaTypes.REGEXP_ARRAY_OR_EXPRESSION.withProps(props)
                        : ParamMetaTypes.REGEXP_ARRAY_OR_EXPRESSION;
            }
        };

        private final String displayName;
        private final YamlBaseType yamlBaseType;

        WellKnown(@NotNull String displayName, @NotNull YamlBaseType yamlBaseType) {
            this.displayName = displayName;
            this.yamlBaseType = yamlBaseType;
        }

        @Override
        public @NotNull String displayName() {
            return displayName;
        }

        @Override
        public @NotNull YamlBaseType yamlBaseType() {
            return yamlBaseType;
        }
    }

    record Custom(@NotNull String displayName,
                  @NotNull YamlBaseType yamlBaseType) implements ConcordType {
    }

    Map<String, ConcordType> ALIASES = Map.ofEntries(
            Map.entry("string", WellKnown.STRING),
            Map.entry("boolean", WellKnown.BOOLEAN),
            Map.entry("int", WellKnown.INTEGER),
            Map.entry("integer", WellKnown.INTEGER),
            Map.entry("number", WellKnown.INTEGER),
            Map.entry("object", WellKnown.OBJECT),
            Map.entry("regexp", WellKnown.REGEXP),
            Map.entry("any", WellKnown.ANY)
    );

    /**
     * Resolves a type name to a known ConcordType.
     * Handles aliases case-insensitively (e.g., "int" → INTEGER, "number" → INTEGER).
     *
     * @return the matching ConcordType, or null if the type name is not recognized
     */
    static @Nullable ConcordType fromString(@NotNull String name) {
        return ALIASES.get(name.toLowerCase());
    }

    /**
     * Resolves a type name to a ConcordType, creating a {@link Custom} instance
     * for unrecognized types. The {@code fallbackBase} determines the structural
     * YAML type used for validation and meta type mapping of unknown types.
     *
     * @param name         the type name to resolve
     * @param fallbackBase the YAML base type to use if the name is not recognized
     * @return the matching known ConcordType, or a Custom instance for unknown types
     */
    static @NotNull ConcordType resolve(@NotNull String name,
                                        @NotNull YamlBaseType fallbackBase) {
        var known = fromString(name);
        if (known != null) {
            return known;
        }
        return new Custom(name, fallbackBase);
    }
}