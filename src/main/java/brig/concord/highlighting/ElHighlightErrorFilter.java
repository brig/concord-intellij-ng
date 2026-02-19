package brig.concord.highlighting;

import brig.concord.el.ElLanguage;
import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;

/**
 * Suppresses the default (raw) error highlighting for EL parser errors.
 * The {@link ElErrorAnnotator} re-annotates them with simplified messages.
 */
public class ElHighlightErrorFilter extends HighlightErrorFilter {

    @Override
    public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement error) {
        return error.getLanguage() != ElLanguage.INSTANCE;
    }
}