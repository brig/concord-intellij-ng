// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta;

import brig.concord.psi.ConcordFile;
import brig.concord.yaml.meta.impl.YamlMetaTypeProvider;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.ModelAccess;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Service(Service.Level.PROJECT)
public final class ConcordMetaTypeProvider extends YamlMetaTypeProvider {

    private static final ModelAccess modelAccess = document -> {
        String filename = Optional.ofNullable(document.getContainingFile())
                .map(PsiFileSystemItem::getName)
                .orElse("concord-file");
        YamlMetaType root = ConcordFileMetaType.getInstance();
        return new Field(filename, root);
    };

    public ConcordMetaTypeProvider(Project project) {
        super(modelAccess, PsiManager.getInstance(project).getModificationTracker());
    }

    public static ConcordMetaTypeProvider getInstance(@NotNull Project project) {
        return project.getService(ConcordMetaTypeProvider.class);
    }

    public @Nullable YamlMetaType getResolvedMetaType(@NotNull PsiElement element) {
        MetaTypeProxy metaTypeProxy = getMetaTypeProxy(element);
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

        if (result.getMetaType() instanceof DynamicMetaType dynamicMetaType) {
            var resolved = dynamicMetaType.resolve(element);
            if (resolved != null) {
                return of(result, resolved);
            }
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
}
