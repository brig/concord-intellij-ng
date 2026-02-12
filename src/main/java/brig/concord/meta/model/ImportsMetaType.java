package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

public class ImportsMetaType extends YamlArrayType {

    private static final ImportsMetaType INSTANCE = new ImportsMetaType();

    public static ImportsMetaType getInstance() {
        return INSTANCE;
    }

    public ImportsMetaType() {
        super(ImportElementMetaType.getInstance());

        setDescriptionKey("doc.imports.description");
    }
}
