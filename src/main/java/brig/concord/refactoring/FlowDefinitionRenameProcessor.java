package brig.concord.refactoring;

import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Handles renaming of flow definitions across multiple files in the same Concord scope.
 * When a flow is renamed, all flow definitions with the same name in the scope are also renamed.
 */
public class FlowDefinitionRenameProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof YAMLKeyValue keyValue
                && ProcessDefinition.isFlowDefinition(keyValue);
    }

    @Override
    public void prepareRenaming(@NotNull PsiElement element,
                                @NotNull String newName,
                                @NotNull Map<PsiElement, String> allRenames) {
        if (!(element instanceof YAMLKeyValue keyValue)) {
            return;
        }

        var process = ProcessDefinitionProvider.getInstance().get(keyValue);
        var flowName = keyValue.getKeyText();
        for (var flow : process.flows(flowName)) {
            if (!flow.equals(element)) {
                allRenames.put(flow, newName);
            }
        }
    }
}
