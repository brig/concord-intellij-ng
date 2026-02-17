package brig.concord.psi;

import brig.concord.psi.ElPropertyProvider.PropertyItem;
import brig.concord.psi.VariablesProvider.Variable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

interface ElPropertySubProvider {

    boolean supports(@NotNull Variable variable);

    @NotNull List<PropertyItem> getProperties(@NotNull Variable variable,
                                              @NotNull List<String> intermediateSegments,
                                              @NotNull PsiElement yamlContext);

    @Nullable PsiElement resolveProperty(@NotNull Variable variable,
                                         @NotNull List<String> intermediateSegments,
                                         @NotNull String propertyName,
                                         @NotNull PsiElement yamlContext);
}
