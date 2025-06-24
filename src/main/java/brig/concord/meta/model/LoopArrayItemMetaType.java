package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlAnything;

public class LoopArrayItemMetaType extends YamlAnything {

    private static final LoopArrayItemMetaType INSTANCE = new LoopArrayItemMetaType();

    public static LoopArrayItemMetaType getInstance() {
        return INSTANCE;
    }
}
