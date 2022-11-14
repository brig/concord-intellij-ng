package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DirImportEntryMetaType extends ConcordMetaType {

    private static final DirImportEntryMetaType INSTANCE = new DirImportEntryMetaType();

    public static DirImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("src");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "src", YamlStringType::getInstance,
            "dest", YamlStringType::getInstance
            );

    protected DirImportEntryMetaType() {
        super("dir import entry");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }
}
