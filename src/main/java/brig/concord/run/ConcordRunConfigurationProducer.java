package brig.concord.run;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import brig.concord.psi.ProcessDefinition;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConcordRunConfigurationProducer extends LazyRunConfigurationProducer<ConcordRunConfiguration> {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConcordRunConfigurationType.getInstance().getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull ConcordRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        var flowInfo = findFlowAtContext(context);
        if (flowInfo == null) {
            return false;
        }

        configuration.setEntryPoint(flowInfo.flowName);
        configuration.setWorkingDirectory(flowInfo.workingDirectory);
        configuration.setName(flowInfo.flowName);

        sourceElement.set(flowInfo.element);
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull ConcordRunConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        var flowInfo = findFlowAtContext(context);
        if (flowInfo == null) {
            return false;
        }

        return flowInfo.flowName.equals(configuration.getEntryPoint())
                && flowInfo.workingDirectory.equals(configuration.getWorkingDirectory());
    }

    private static @Nullable FlowInfo findFlowAtContext(@NotNull ConfigurationContext context) {
        var location = context.getPsiLocation();
        if (location == null) {
            return null;
        }

        var containingFile = location.getContainingFile();
        if (!(containingFile instanceof ConcordFile)) {
            return null;
        }

        var flowKeyValue = ProcessDefinition.findEnclosingFlowDefinition(location);
        if (flowKeyValue == null) {
            return null;
        }

        var virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }

        var flowName = flowKeyValue.getKeyText();
        var workingDirectory = getWorkingDirectory(context.getProject(), virtualFile);

        return new FlowInfo(flowKeyValue, flowName, workingDirectory);
    }

    static @NotNull String getWorkingDirectory(@NotNull Project project,
                                               @NotNull VirtualFile virtualFile) {
        var primaryScope = ConcordScopeService.getInstance(project).getPrimaryScope(virtualFile);
        if (primaryScope != null) {
            return primaryScope.getRootDir().toString();
        }
        // Fallback to parent directory
        var parent = virtualFile.getParent();
        return parent != null ? parent.getPath() : "";
    }

    private record FlowInfo(@NotNull PsiElement element,
                            @NotNull String flowName,
                            @NotNull String workingDirectory) {
    }
}
