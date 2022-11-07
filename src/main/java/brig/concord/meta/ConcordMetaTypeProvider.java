package brig.concord.meta;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.YamlDebugUtil;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
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
import java.util.function.Supplier;

@Service
public final class ConcordMetaTypeProvider extends YamlMetaTypeProvider {

    private static final Logger LOG = Logger.getInstance(ConcordMetaTypeProvider.class);

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
        debug(() -> " >> computing meta type proxy for : " + YamlDebugUtil.getDebugInfo(element));
        MetaTypeProxy computed = super.getMetaTypeProxy(element);
        debug(() -> " << finished for : " + YamlDebugUtil.getDebugInfo(element) +
                ", result: " + (computed == null ? "<null>" : computed));
        return computed;
    }

    @Nullable
    public MetaTypeProxy getValueMetaType(@NotNull YAMLValue typedValue) {
        debug(() -> " >> computing value meta type for : " + YamlDebugUtil.getDebugInfo(typedValue));
        MetaTypeProxy result = super.getValueMetaType(typedValue);
        debug(() -> " << finished for : " + YamlDebugUtil.getDebugInfo(typedValue) +
                ", result: " + (result == null ? "<null>" : result));
        return result;
    }

    @Nullable
    @Override
    public YAMLValue getMetaOwner(@NotNull PsiElement psi) {
        debug(() -> " >> getting meta owner for : " + YamlDebugUtil.getDebugInfo(psi));
        YAMLValue result = PsiTreeUtil.getParentOfType(psi, YAMLValue.class, false);
        debug(() -> " << finished for : " + YamlDebugUtil.getDebugInfo(psi) +
                ", result: " + (result == null ? "<null>" : result));
        return result;
    }

    private static void debug(Supplier<String> textSupplier) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(textSupplier.get());
        }
    }
}
