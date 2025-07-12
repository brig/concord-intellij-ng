package brig.concord.psi.impl;

import brig.concord.ConcordFileType;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.YAMLElementTypes;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.impl.YAMLFileImpl;

import java.util.Optional;

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

    @Override
    public Optional<YAMLDocument> getDocument() {
        var documentNode = getNode().findChildByType(YAMLElementTypes.DOCUMENT);
        return documentNode != null ? Optional.of((YAMLDocument) documentNode.getPsi()) : Optional.empty();
    }

    @Override
    public Optional<YAMLKeyValue> configuration() {
        return topLevelKv("configuration");
    }

    @Override
    public Optional<YAMLKeyValue> flows() {
        return topLevelKv("flows");
    }

    @Override
    public Optional<YAMLKeyValue> forms() {
        return topLevelKv("forms");
    }

    @Override
    public Optional<YAMLKeyValue> profiles() {
        return topLevelKv("profiles");
    }

    @Override
    public Optional<YAMLKeyValue> resources() {
        return topLevelKv("resources");
    }

    @Override
    public Optional<YAMLKeyValue> imports() {
        return topLevelKv("imports");
    }

    @Override
    public Optional<YAMLKeyValue> publicFlows() {
        return topLevelKv("publicFlows");
    }

    @Override
    public Optional<YAMLKeyValue> triggers() {
        return topLevelKv("triggers");
    }

    private Optional<YAMLKeyValue> topLevelKv(String key) {
        return getDocument()
                .map(YAMLDocument::getTopLevelValue)
                .map(v -> ObjectUtils.tryCast(v, YAMLMapping.class))
                .map(m -> m.getKeyValueByKey(key));
    }
}
