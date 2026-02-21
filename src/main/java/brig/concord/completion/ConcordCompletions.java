// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.completion;

import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.impl.YamlMetaTypeCompletionProviderBase;
import brig.concord.yaml.meta.impl.YamlMetaTypeProvider;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class ConcordCompletions extends CompletionContributor {

    public ConcordCompletions() {
        extend(CompletionType.BASIC, psiElement(), new KeyCompletion());
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

            if (!(params.getOriginalFile() instanceof ConcordFile)) {
                return;
            }

            if (ConcordScopeService.getInstance(params.getOriginalFile().getProject()).isIgnored(params.getOriginalFile())) {
                return;
            }

            super.addCompletions(params, context, result);
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
