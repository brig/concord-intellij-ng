package brig.concord.documentation;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Documented {

    default @Nullable String getDescription() {
        return null;
    }

    default @Nullable String getDocumentationExample() {
        return null;
    }

    default @NotNull List<DocumentedField> getDocumentationFields() {
        return List.of();
    }

    default @NotNull List<DocumentedField> getValues() {
        return List.of();
    }

    default @Nullable String getDocumentationBody(@NotNull PsiElement element) {
        return null;
    }

    record DocumentedField(String name, @Nullable String typeDisplayName, boolean required,
                           @Nullable String description, @NotNull List<DocumentedField> children) {
    }
}
