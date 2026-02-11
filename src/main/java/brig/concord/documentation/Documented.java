package brig.concord.documentation;

import brig.concord.ConcordBundle;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Documented {

    default @Nullable String getDocBundlePrefix() {
        return null;
    }

    default @Nullable String getDescription() {
        var prefix = getDocBundlePrefix();
        return prefix != null ? ConcordBundle.findMessage(prefix + ".description") : null;
    }

    default @Nullable String getDocumentationExample() {
        return null;
    }

    default @NotNull List<DocumentedField> getDocumentationFields() {
        return List.of();
    }

    default @Nullable String getDocumentationBody(@NotNull PsiElement element) {
        return null;
    }

    record DocumentedField(String name, @Nullable String typeDisplayName, boolean required,
                           @Nullable String description, @NotNull List<DocumentedField> children) {
        public DocumentedField(String name, String typeDisplayName, boolean required, @Nullable String description) {
            this(name, typeDisplayName, required, description, List.of());
        }
    }
}
