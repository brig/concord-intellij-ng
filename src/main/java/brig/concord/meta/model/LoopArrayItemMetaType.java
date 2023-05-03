package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlAnything;

@SuppressWarnings("UnstableApiUsage")
public class LoopArrayItemMetaType extends YamlAnything {

    private static final LoopArrayItemMetaType INSTANCE = new LoopArrayItemMetaType();

    public static LoopArrayItemMetaType getInstance() {
        return INSTANCE;
    }
}
