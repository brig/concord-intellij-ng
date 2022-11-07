package brig.concord.completion;

import brig.concord.meta.ConcordMetaTypeProvider;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeCompletionProviderBase;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeProvider;

import java.util.HashMap;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class ConcordCompletions extends CompletionContributor {

    private static final HashMap<PsiElement, CompletionParameters> completionParametersHashMap = new HashMap<>();

    public ConcordCompletions() {
        extend(CompletionType.BASIC, psiElement(), new KeyCompletion());
    }

    public static CompletionParameters getCompletionParameters(PsiElement element) {
        return completionParametersHashMap.get(element);
    }

    public static void registerCompletionParameters(CompletionParameters parameters) {
        completionParametersHashMap.put(parameters.getPosition(), parameters);
    }

    public static PsiElement getPlaceholderToken() {
        return completionParametersHashMap.keySet().stream().findFirst().orElse(null);
    }

    public static void removeCompletionParameters(CompletionParameters parameters) {
        completionParametersHashMap.remove(parameters.getPosition());
    }

    static class KeyCompletion extends YamlMetaTypeCompletionProviderBase {

        @Override
        protected @Nullable YamlMetaTypeProvider getMetaTypeProvider(@NotNull CompletionParameters params) {
            return ConcordMetaTypeProvider.getInstance(params.getPosition().getProject());
        }

        @Override
        protected void addCompletions(@NotNull CompletionParameters params,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            ConcordCompletions.registerCompletionParameters(params);
            super.addCompletions(params, context, result);
            ConcordCompletions.removeCompletionParameters(params);
        }

    }
}
