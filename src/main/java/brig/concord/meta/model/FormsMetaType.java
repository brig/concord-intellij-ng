package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlArrayType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

public class FormsMetaType extends MapMetaType {

    private static final FormsMetaType INSTANCE = new FormsMetaType();

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

    private static class FieldsWrapper extends MapMetaType {

        private static final FieldsWrapper INSTANCE = new FieldsWrapper();

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
