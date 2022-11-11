package brig.concord.meta.model;

import java.util.List;

public class ImportElementMetaType extends IdentityElementMetaType {

    private static final List<IdentityMetaType> entries = List.of(
            DirImportMetaType.getInstance(),
            GitImportMetaType.getInstance()
    );

    private static final ImportElementMetaType INSTANCE = new ImportElementMetaType();

    public static ImportElementMetaType getInstance() {
        return INSTANCE;
    }

    protected ImportElementMetaType() {
        super("Imports", entries);
    }
}
