// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ProcessDefinitionProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class ConcordRequiredFlowParameters {

    private ConcordRequiredFlowParameters() {
    }

    public static @NotNull List<String> findMissingRequiredParameters(@NotNull ConcordRunConfiguration configuration) {
        return ReadAction.compute(() -> doFindMissingRequiredParameters(configuration));
    }

    private static @NotNull List<String> doFindMissingRequiredParameters(@NotNull ConcordRunConfiguration configuration) {
        var project = configuration.getProject();
        var runModeSettings = ConcordRunModeSettings.getInstance(project);

        var buildResult = ConcordCommandLineBuilder.buildParameters(
                configuration.getEntryPoint(),
                configuration.getParameters(),
                runModeSettings.isDelegatingMode(),
                runModeSettings.getMainEntryPoint(),
                runModeSettings.getFlowParameterName(),
                runModeSettings.getDefaultParameters(),
                runModeSettings.getActiveProfiles()
        );

        var targetFlowName = resolveTargetFlowName(configuration, runModeSettings, buildResult.parameters());
        if (targetFlowName == null || targetFlowName.isBlank()) {
            return List.of();
        }

        var requiredParameters = findRequiredInputParameters(project, configuration.getWorkingDirectory(), targetFlowName);
        if (requiredParameters.isEmpty()) {
            return List.of();
        }

        var effectiveParams = buildResult.parameters();
        var missing = new ArrayList<String>();
        for (var parameterName : requiredParameters) {
            var value = effectiveParams.get(parameterName);
            if (value == null || value.isBlank()) {
                missing.add(parameterName);
            }
        }

        return List.copyOf(missing);
    }

    private static @Nullable String resolveTargetFlowName(@NotNull ConcordRunConfiguration configuration,
                                                          @NotNull ConcordRunModeSettings runModeSettings,
                                                          @NotNull Map<String, String> effectiveParameters) {
        if (runModeSettings.isDelegatingMode()) {
            var flowParameterName = runModeSettings.getFlowParameterName();
            if (flowParameterName.isBlank()) {
                return null;
            }
            return effectiveParameters.get(flowParameterName);
        }
        return configuration.getEntryPoint();
    }

    private static @NotNull List<String> findRequiredInputParameters(@NotNull Project project,
                                                                      @NotNull String workingDirectory,
                                                                      @NotNull String flowName) {
        var contextFile = resolveContextFile(workingDirectory);
        if (contextFile == null) {
            return List.of();
        }

        var psiFile = PsiManager.getInstance(project).findFile(contextFile);
        if (psiFile == null) {
            return List.of();
        }

        var flowDefinition = ProcessDefinitionProvider.getInstance().get(psiFile).flow(flowName);
        if (flowDefinition == null) {
            return List.of();
        }

        var flowDoc = FlowCallParamsProvider.findFlowDocumentationBefore(flowDefinition);
        if (flowDoc == null) {
            return List.of();
        }

        var requiredNames = new LinkedHashSet<String>();
        for (var parameter : flowDoc.getInputParameters()) {
            if (parameter.isMandatory()) {
                var name = parameter.getName().trim();
                if (!name.isBlank()) {
                    requiredNames.add(name);
                }
            }
        }

        return List.copyOf(requiredNames);
    }

    private static @Nullable VirtualFile resolveContextFile(@NotNull String workingDirectory) {
        if (workingDirectory.isBlank()) {
            return null;
        }

        var dir = LocalFileSystem.getInstance().findFileByPath(workingDirectory);
        if (dir == null && ApplicationManager.getApplication().isUnitTestMode()) {
            dir = VirtualFileManager.getInstance().findFileByUrl("temp://" + workingDirectory);
        }
        if (dir == null) {
            return null;
        }

        return ConcordFile.findRootFile(dir);
    }
}
