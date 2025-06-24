package brig.concord.psi.impl;

import brig.concord.ConcordFileType;
import brig.concord.psi.ConcordFile;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.impl.YAMLFileImpl;

public class ConcordFileImpl extends YAMLFileImpl implements ConcordFile {

    public ConcordFileImpl(FileViewProvider viewProvider) {
        super(viewProvider);
    }

    @Override
    public @NotNull FileType getFileType() {
        return ConcordFileType.INSTANCE;
    }

    @Override
    @NotNull
    public SearchScope getUseScope() {
        return GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(getProject()), ConcordFileType.INSTANCE);
    }
}
