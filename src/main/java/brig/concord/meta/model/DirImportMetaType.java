package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DirImportMetaType extends IdentityMetaType {

    private static final DirImportMetaType INSTANCE = new DirImportMetaType();

    public static DirImportMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "src", YamlStringType::getInstance,
            "dest", YamlStringType::getInstance
            );

    protected DirImportMetaType() {
        super("dir", "dir", Set.of("dir", "src"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
