package brig.concord.psi.impl;

import brig.concord.ConcordFileType;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.YAMLElementTypes;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
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
    public Optional<YAMLSequence> triggers() {
        return topLevelValue("triggers", YAMLSequence.class);
    }

    @Override
    public Optional<YAMLKeyValue> triggersKv() {
        return topLevelKv("triggers");
    }

    private <T extends PsiElement> Optional<T> topLevelValue(@NotNull String key, @NotNull Class<T> type) {
        return topLevelKv(key)
                .map(YAMLKeyValue::getValue)
                .filter(type::isInstance)
                .map(type::cast);
    }

    private Optional<YAMLKeyValue> topLevelKv(@NotNull String key) {
        return getDocument()
                .map(YAMLDocument::getTopLevelValue)
                .map(v -> ObjectUtils.tryCast(v, YAMLMapping.class))
                .map(m -> m.getKeyValueByKey(key));
    }
}
