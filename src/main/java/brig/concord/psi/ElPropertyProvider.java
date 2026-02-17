package brig.concord.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ElPropertyProvider {

    record PropertyItem(@NotNull String name, @NotNull String source,
                        @Nullable String description, @Nullable PsiElement declaration) {}

    @NotNull List<PropertyItem> getProperties(@NotNull List<String> accessChain, @NotNull PsiElement yamlContext);

    @Nullable PsiElement resolveProperty(@NotNull List<String> accessChain,
                                         @NotNull String propertyName,
                                         @NotNull PsiElement yamlContext);

    static @NotNull ElPropertyProvider getInstance(@NotNull Project project) {
        return project.getService(ElPropertyProvider.class);
    }
}
