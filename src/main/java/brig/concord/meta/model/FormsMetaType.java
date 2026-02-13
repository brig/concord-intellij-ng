package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.MapMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class FormsMetaType extends MapMetaType implements HighlightProvider {

    private static final FormsMetaType INSTANCE = new FormsMetaType();

    private static final List<Field> defaultCompletions = List.of(new Field("myForm", FormFieldsMetaType.getInstance()));

    public static FormsMetaType getInstance() {
        return INSTANCE;
    }

    private FormsMetaType() {
        super(desc("doc.forms.description"));
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

    @Override
    public @Nullable String getDocumentationExample() {
        return """
                forms:
                  myForm:
                    - myValue: { type: "string", label: "Value" }
                
                flows:
                  default:
                    - form: myForm
                """;
    }

    private static class FieldsWrapper extends MapMetaType implements HighlightProvider {

        private static final FieldsWrapper INSTANCE = new FieldsWrapper();

        private static final List<Field> defaultCompletions = List.of(new Field("myField", FormFieldMetaType.getInstance()));

        public static FieldsWrapper getInstance() {
            return INSTANCE;
        }

        private FieldsWrapper() {
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
            super(FieldsWrapper.getInstance(), desc("doc.forms.formName.description"));
        }
    }
}
