package brig.concord.meta.model;

import brig.concord.completion.ConcordCompletions;
import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.CompletionContext;
import org.jetbrains.yaml.meta.model.YamlStringType;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CallMetaType extends YamlStringType {

    private static final CallMetaType INSTANCE = new CallMetaType();

    public static CallMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        VirtualFile vFile = Optional.ofNullable(ConcordCompletions.getPlaceholderToken())
                .map(ConcordCompletions::getCompletionParameters)
                .map(CompletionParameters::getOriginalFile)
                .map(PsiFile::getVirtualFile)
                .filter(VirtualFile::isValid)
                .orElse(null);
        if (vFile == null) {
            return Collections.emptyList();
        }

        PsiFile psiFile = PsiManager.getInstance(insertedScalar.getProject()).findFile(vFile);

        ProcessDefinition processDefinition = ProcessDefinitionProvider.getInstance().get(psiFile);
        if (processDefinition == null) {
            return Collections.emptyList();
        }

        return processDefinition.flowNames().stream()
                .sorted()
                .map(name -> LookupElementBuilder.create(name)
                        .withPresentableText(name))
                .collect(Collectors.toList());
    }
}
