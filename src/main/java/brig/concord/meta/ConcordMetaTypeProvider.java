package brig.concord.meta;

import brig.concord.meta.model.IdentityElementMetaType;
import brig.concord.meta.model.IdentityMetaType;
import brig.concord.psi.YamlDebugUtil;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeProvider;
import org.jetbrains.yaml.meta.model.*;
import org.jetbrains.yaml.psi.*;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@SuppressWarnings("UnstableApiUsage")
public final class ConcordMetaTypeProvider extends YamlMetaTypeProvider {

    private static final Logger LOG = Logger.getInstance(ConcordMetaTypeProvider.class);

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

        if (result.getMetaType() instanceof YamlAnyOfType anyOfType) {
            if (element instanceof YAMLSequence) {
                for (YamlMetaType t : anyOfType.getSubTypes()) {
                    if (t instanceof YamlArrayType) {
                        return of(result, t);
                    }
                }
            } else if (element instanceof YAMLScalar) {
                for (YamlMetaType t : anyOfType.getSubTypes()) {
                    if (t instanceof YamlScalarType) {
                        return of(result, t);
                    }
                }
            }
        }

        return result;
    }

    @Nullable
    public MetaTypeProxy getValueMetaType(@NotNull YAMLValue typedValue) {
        debug(() -> " >> computing value meta type for : " + YamlDebugUtil.getDebugInfo(typedValue));
        MetaTypeProxy result = super.getValueMetaType(typedValue);
        debug(() -> " << finished for : " + YamlDebugUtil.getDebugInfo(typedValue) +
                ", result: " + (result == null ? "<null>" : result + " " + result.getMetaType().getClass()));
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
