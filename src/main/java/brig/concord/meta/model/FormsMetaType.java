package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;

import java.util.List;

public class FormsMetaType extends MapMetaType implements HighlightProvider {

    private static final FormsMetaType INSTANCE = new FormsMetaType();

    private static final List<Field> defaultCompletions = List.of(new Field("myForm", FormFieldsMetaType.getInstance()));

    protected FormsMetaType() {
        super("Form definition");
    }

    public static FormsMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return FormFieldsMetaType.getInstance();
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        return defaultCompletions;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KIND;
    }

    private static class FieldsWrapper extends MapMetaType implements HighlightProvider {

        private static final FieldsWrapper INSTANCE = new FieldsWrapper();

        private static final List<Field> defaultCompletions = List.of(new Field("myField", FormFieldMetaType.getInstance()));

        public static FieldsWrapper getInstance() {
            return INSTANCE;
        }

        protected FieldsWrapper() {
            super("Form fields");
        }

        @Override
        protected YamlMetaType getMapEntryType(String name) {
            return FormFieldMetaType.getInstance();
        }

        @Override
        public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
            return defaultCompletions;
        }

        @Override
        public @Nullable TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_KIND;
        }
    }

    private static class FormFieldsMetaType extends YamlArrayType {

        private static final FormFieldsMetaType INSTANCE = new FormFieldsMetaType();

        public static FormFieldsMetaType getInstance() {
            return INSTANCE;
        }

        public FormFieldsMetaType() {
            super(FieldsWrapper.getInstance());
        }
    }
}
