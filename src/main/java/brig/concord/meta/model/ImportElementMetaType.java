package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ImportElementMetaType extends IdentityElementMetaType {

    private static final List<IdentityMetaType> entries = List.of(
            new ImportMetaType("dir", DirImportEntryMetaType::getInstance),
            new ImportMetaType("git", GitImportEntryMetaType::getInstance),
            new ImportMetaType("mvn", MvnImportEntryMetaType::getInstance)
    );

    private static final ImportElementMetaType INSTANCE = new ImportElementMetaType();

    public static ImportElementMetaType getInstance() {
        return INSTANCE;
    }

    protected ImportElementMetaType() {
        super("Imports", entries);
    }

    private static class ImportMetaType extends IdentityMetaType {

        private final Map<String, Supplier<YamlMetaType>> features;

        protected ImportMetaType(String identity, Supplier<YamlMetaType> entry) {
            super(identity, identity, Set.of(identity));

            this.features = Map.of(identity, entry);
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
    }
}
