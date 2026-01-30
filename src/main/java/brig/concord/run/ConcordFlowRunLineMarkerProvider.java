package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.ConcordIcons;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static brig.concord.psi.ProcessDefinition.isFlowDefinition;

public final class ConcordFlowRunLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof YAMLKeyValue keyValue)) {
            return null;
        }

        var key = keyValue.getKey();
        if (key == null) {
            return null;
        }

        var containingFile = element.getContainingFile();
        if (!(containingFile instanceof ConcordFile)) {
            return null;
        }

        if (!isFlowDefinition(keyValue)) {
            return null;
        }

        var service = ConcordScopeService.getInstance(element.getProject());
        var vFile = containingFile.getVirtualFile();
        if (vFile == null || service.isOutOfScope(vFile) || service.isIgnored(vFile)) {
            return null;
        }

        var flowName = keyValue.getKeyText();

        return new LineMarkerInfo<>(
                key,
                key.getTextRange(),
                ConcordIcons.RUN,
                e -> ConcordBundle.message("run.flow.action.text", flowName),
                (e, elt) -> runFlow(elt, flowName),
                GutterIconRenderer.Alignment.LEFT,
                () -> ConcordBundle.message("run.flow.action.description")
        );
    }

    private static void runFlow(@NotNull PsiElement element, @NotNull String flowName) {
        var project = element.getProject();
        var psiFile = element.getContainingFile();
        if (psiFile == null) {
            return;
        }

        var virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return;
        }

        var workingDirectory = ConcordRunConfigurationHelper.getWorkingDirectory(project, virtualFile);
        ConcordRunConfigurationHelper.createAndRun(project, flowName, workingDirectory);
    }
}
