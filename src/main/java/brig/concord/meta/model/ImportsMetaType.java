package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ImportsMetaType extends YamlArrayType {

    private static final ImportsMetaType INSTANCE = new ImportsMetaType();

    public static ImportsMetaType getInstance() {
        return INSTANCE;
    }

    public ImportsMetaType() {
        super(ImportElementMetaType.getInstance(), descKey("doc.imports.description"));
    }
}
