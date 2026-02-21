// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.ProcessDefinition;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

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

        var project = context.getProject();
        var runModeSettings = ConcordRunModeSettings.getInstance(project);

        if (runModeSettings.isDelegatingMode()) {
            // Delegating: entry-point = main, flow passed as parameter
            configuration.setEntryPoint(runModeSettings.getMainEntryPoint());
            configuration.setWorkingDirectory(flowInfo.workingDirectory);

            var params = new LinkedHashMap<>(configuration.getParameters());
            params.put(runModeSettings.getFlowParameterName(), flowInfo.flowName);
            configuration.setParameters(params);

            configuration.setName(runModeSettings.getMainEntryPoint() + " (" + flowInfo.flowName + ")");
        } else {
            // Direct: entry-point = flowName
            configuration.setEntryPoint(flowInfo.flowName);
            configuration.setWorkingDirectory(flowInfo.workingDirectory);
            configuration.setName(flowInfo.flowName);
        }

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

        return ConcordRunConfigurationHelper.isMatchingConfiguration(
                configuration, flowInfo.flowName, flowInfo.workingDirectory, context.getProject());
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
        var workingDirectory = ConcordRunConfigurationHelper.getWorkingDirectory(context.getProject(), virtualFile);

        return new FlowInfo(flowKeyValue, flowName, workingDirectory);
    }

    private record FlowInfo(@NotNull PsiElement element,
                            @NotNull String flowName,
                            @NotNull String workingDirectory) {
    }
}
