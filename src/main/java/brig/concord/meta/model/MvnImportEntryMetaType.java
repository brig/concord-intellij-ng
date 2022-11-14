package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MvnImportEntryMetaType extends ConcordMetaType {

    private static final MvnImportEntryMetaType INSTANCE = new MvnImportEntryMetaType();

    public static MvnImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("url");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "url", YamlStringType::getInstance,
            "dest", YamlStringType::getInstance
    );

    protected MvnImportEntryMetaType() {
        super("mvn import entry");
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
