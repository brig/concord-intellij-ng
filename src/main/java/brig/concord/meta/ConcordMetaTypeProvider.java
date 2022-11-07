package brig.concord.meta;

import brig.concord.psi.ConcordFile;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeProvider;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.ModelAccess;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Optional;

@Service
public final class ConcordMetaTypeProvider extends YamlMetaTypeProvider {

    private static final Key<CachedValue<MetaTypeProxy>> VALUE_META_TYPE = new Key<>("VALUE_META_TYPE");
    private static final Key<CachedValue<MetaTypeProxy>> KEY_VALUE_META_TYPE = new Key<>("KEY_VALUE_META_TYPE");
    private static final Key<CachedValue<MetaTypeProxy>> META_TYPE_PROXY = new Key<>("META_TYPE_PROXY");

    private static final ModificationTracker YAML_MODIFICATION_TRACKER = ModificationTracker.NEVER_CHANGED;

    private static final ModelAccess modelAccess = document -> {
        String filename = Optional.ofNullable(document.getContainingFile())
                .map(PsiFileSystemItem::getName)
                .orElse("concord-file");
        final YamlMetaType root;
        if (ConcordFile.isRootFileName(filename)) {
            root = ConcordFileMetaType.getInstance();
        } else {
            root = ConcordFileMetaType.getInstance();
        }
        return new Field(filename, root);
    };

    public ConcordMetaTypeProvider() {
        super(modelAccess, YAML_MODIFICATION_TRACKER);
    }

    public static ConcordMetaTypeProvider getInstance(@NotNull Project project) {
        return project.getService(ConcordMetaTypeProvider.class);
    }

    public @Nullable YamlMetaType getResolvedKeyValueMetaTypeMeta(@NotNull YAMLKeyValue keyValue) {
        return Optional.ofNullable(getKeyValueMetaType(keyValue))
                .map(MetaTypeProxy::getMetaType)
                .orElse(null);
    }

    public @Nullable YamlMetaType getResolvedMetaType(@NotNull PsiElement element) {
        final MetaTypeProxy metaTypeProxy = getMetaTypeProxy(element);
        return metaTypeProxy != null ? metaTypeProxy.getMetaType() : null;
    }

    @Nullable
    @Override
    public MetaTypeProxy getMetaTypeProxy(@NotNull PsiElement element) {
        return CachedValuesManager.getCachedValue(element, META_TYPE_PROXY, () ->
                new CachedValueProvider.Result<>(super.getMetaTypeProxy(element), element.getContainingFile()));
    }

    @Nullable
    @Override
    public YAMLValue getMetaOwner(@NotNull PsiElement psi) {
        return PsiTreeUtil.getParentOfType(psi, YAMLValue.class, false);
    }
}
