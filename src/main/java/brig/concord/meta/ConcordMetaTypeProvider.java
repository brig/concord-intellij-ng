package brig.concord.meta;

import brig.concord.meta.model.IdentityElementMetaType;
import brig.concord.meta.model.IdentityMetaType;
import brig.concord.psi.ConcordFile;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
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
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@SuppressWarnings("UnstableApiUsage")
public final class ConcordMetaTypeProvider extends YamlMetaTypeProvider {

    private static final Logger LOG = Logger.getInstance(ConcordMetaTypeProvider.class);

    private static final Key<CachedValue<MetaTypeProxy>> META_TYPE_PROXY = new Key<>("META_TYPE_PROXY");

    private static final ModificationTracker YAML_MODIFICATION_TRACKER = ModificationTracker.NEVER_CHANGED;

    private static final ModelAccess modelAccess = document -> {
        String filename = Optional.ofNullable(document.getContainingFile())
                .map(PsiFileSystemItem::getName)
                .orElse("concord-file");
        YamlMetaType root = ConcordFileMetaType.getInstance();
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
                .map(YamlMetaTypeProvider.MetaTypeProxy::getMetaType)
                .orElse(null);
    }

    public @Nullable YamlMetaType getResolvedMetaType(@NotNull PsiElement element) {
        final MetaTypeProxy metaTypeProxy = getMetaTypeProxy(element);
        return metaTypeProxy != null ? metaTypeProxy.getMetaType() : null;
    }

    private static MetaTypeProxy of(MetaTypeProxy result, YamlMetaType newType) {
        return FieldAndRelation.forNullable(new Field(result.getField().getName(), newType), result.getField().getDefaultRelation());
    }

    @Nullable
    @Override
    public MetaTypeProxy getMetaTypeProxy(@NotNull PsiElement element) {
        return CachedValuesManager.getCachedValue(element, META_TYPE_PROXY, () ->
                new CachedValueProvider.Result<>(resolveMetaTypeProxy(element), element.getContainingFile()));
    }

    private MetaTypeProxy resolveMetaTypeProxy(@NotNull PsiElement element) {
        MetaTypeProxy result = super.getMetaTypeProxy(element);
        if (result == null) {
            return null;
        }

        if (result.getMetaType() instanceof IdentityElementMetaType identity) {
            if (element instanceof YAMLMapping m) {
                IdentityMetaType mt = identity.findEntry(m);
                if (mt != null) {
                    return of(result, mt);
                }
            }
        }

        if (result.getMetaType() instanceof DynamicMetaType dynamicMetaType) {
            return of(result, dynamicMetaType.resolve(element));
        }

        return result;
    }

    @Nullable
    public MetaTypeProxy getValueMetaType(@NotNull YAMLValue typedValue) {
        return super.getValueMetaType(typedValue);
    }

    @Nullable
    @Override
    public YAMLValue getMetaOwner(@NotNull PsiElement psi) {
        PsiFile file = psi.getContainingFile();
        if (!(file instanceof ConcordFile)) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(psi, YAMLValue.class, false);
    }

    private static void debug(Supplier<String> textSupplier) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(textSupplier.get());
        }
    }
}
