// SPDX-License-Identifier: Apache-2.0
package brig.concord;

import brig.concord.meta.model.value.AnythingMetaType;
import brig.concord.meta.model.value.AnyOfType;
import brig.concord.meta.model.value.ParamMetaTypes;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ConcordType {

    @NotNull String displayName();

    @NotNull YamlBaseType yamlBaseType();

    private static @NotNull YamlMetaType apply(@Nullable AnyOfType base, @Nullable TypeProps props) {
        if (base == null) {
            return props != null ? new AnythingMetaType(props) : AnythingMetaType.getInstance();
        }
        return props != null ? base.withProps(props) : base;
    }

    default @NotNull YamlMetaType scalarMetaType(@Nullable TypeProps props) {
        return yamlBaseType().scalarMetaType(props);
    }

    default @NotNull AnyOfType arrayMetaType(@Nullable TypeProps props) {
        return yamlBaseType().arrayMetaType(props);
    }

    enum YamlBaseType {
        STRING(ParamMetaTypes.STRING_OR_EXPRESSION, ParamMetaTypes.STRING_ARRAY_OR_EXPRESSION),
        BOOLEAN(ParamMetaTypes.BOOLEAN_OR_EXPRESSION, ParamMetaTypes.BOOLEAN_ARRAY_OR_EXPRESSION),
        INTEGER(ParamMetaTypes.NUMBER_OR_EXPRESSION, ParamMetaTypes.NUMBER_ARRAY_OR_EXPRESSION),
        OBJECT(ParamMetaTypes.OBJECT_OR_EXPRESSION, ParamMetaTypes.OBJECT_ARRAY_OR_EXPRESSION),
        ANY(null, ParamMetaTypes.ARRAY_OR_EXPRESSION);

        private final @Nullable AnyOfType scalarBase;
        private final @NotNull AnyOfType arrayBase;

        YamlBaseType(@Nullable AnyOfType scalarBase, @NotNull AnyOfType arrayBase) {
            this.scalarBase = scalarBase;
            this.arrayBase = arrayBase;
        }

        public @NotNull YamlMetaType scalarMetaType(@Nullable TypeProps props) {
            return ConcordType.apply(scalarBase, props);
        }

        public @NotNull AnyOfType arrayMetaType(@Nullable TypeProps props) {
            return (AnyOfType) ConcordType.apply(arrayBase, props);
        }
    }

    enum WellKnown implements ConcordType {
        STRING("string", YamlBaseType.STRING),
        BOOLEAN("boolean", YamlBaseType.BOOLEAN),
        INTEGER("integer", YamlBaseType.INTEGER),
        OBJECT("object", YamlBaseType.OBJECT),
        ANY("any", YamlBaseType.ANY),
        REGEXP("regexp", YamlBaseType.STRING, ParamMetaTypes.REGEXP_OR_EXPRESSION, ParamMetaTypes.REGEXP_ARRAY_OR_EXPRESSION);

        private final String displayName;
        private final YamlBaseType yamlBaseType;
        private final @Nullable AnyOfType scalarBase;
        private final @Nullable AnyOfType arrayBase;

        WellKnown(String displayName, YamlBaseType base) {
            this(displayName, base, base.scalarBase, base.arrayBase);
        }

        WellKnown(String displayName, YamlBaseType yamlBaseType, @Nullable AnyOfType scalar, @Nullable AnyOfType array) {
            this.displayName = displayName;
            this.yamlBaseType = yamlBaseType;
            this.scalarBase = scalar;
            this.arrayBase = array;
        }

        @Override
        public @NotNull YamlMetaType scalarMetaType(@Nullable TypeProps props) {
            return ConcordType.apply(scalarBase, props);
        }

        @Override
        public @NotNull AnyOfType arrayMetaType(@Nullable TypeProps props) {
            return (AnyOfType) ConcordType.apply(arrayBase, props);
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
}
