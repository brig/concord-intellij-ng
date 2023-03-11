package brig.concord.completion;

import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.psi.ConcordFile;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeCompletionProviderBase;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeProvider;

import java.util.HashMap;
import java.util.List;

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

    @SuppressWarnings("UnstableApiUsage")
    static class KeyCompletion extends YamlMetaTypeCompletionProviderBase {

        @Override
        protected @Nullable YamlMetaTypeProvider getMetaTypeProvider(@NotNull CompletionParameters params) {
            return ConcordMetaTypeProvider.getInstance(params.getPosition().getProject());
        }

        @Override
        protected void addCompletions(@NotNull CompletionParameters params,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            if (!(params.getOriginalFile() instanceof ConcordFile)) {
                return;
            }

            ConcordCompletions.registerCompletionParameters(params);
            try {
                super.addCompletions(params, context, result);
            } finally {
                ConcordCompletions.removeCompletionParameters(params);
            }
        }

        @Override
        protected void registerBasicKeyCompletion(@NotNull CompletionResultSet result, @NotNull List<LookupElementBuilder> lookups, @NotNull InsertHandler<LookupElement> insertHandler) {
            if (!lookups.isEmpty()) {
                lookups.stream().map(l -> {
                    if (l.getInsertHandler() == null) {
                        return l.withInsertHandler(insertHandler);
                    }
                    return l.withInsertHandler(new ChainInsertHandler(List.of(insertHandler, l.getInsertHandler())));
                }).forEach(result::addElement);
            }
        }
    }
}
