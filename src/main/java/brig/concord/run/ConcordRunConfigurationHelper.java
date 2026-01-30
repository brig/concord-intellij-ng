package brig.concord.run;

import brig.concord.psi.ConcordScopeService;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public final class ConcordRunConfigurationHelper {

    private ConcordRunConfigurationHelper() {
    }

    public static @NotNull RunnerAndConfigurationSettings createConfiguration(
            @NotNull Project project,
            @NotNull String flowName,
            @NotNull String workingDirectory) {

        var runModeSettings = ConcordRunModeSettings.getInstance(project);
        var runManager = RunManager.getInstance(project);

        var settings = runManager.createConfiguration(
                flowName,
                ConcordRunConfigurationType.getInstance().getConfigurationFactories()[0]
        );

        var configuration = settings.getConfiguration();
        if (!(configuration instanceof ConcordRunConfiguration concordConfig)) {
            throw new IllegalStateException("Expected ConcordRunConfiguration but got " + configuration.getClass());
        }

        String configName;
        if (runModeSettings.isDelegatingMode()) {
            configName = runModeSettings.getMainEntryPoint() + " (" + flowName + ")";
            concordConfig.setEntryPoint(runModeSettings.getMainEntryPoint());
            concordConfig.setWorkingDirectory(workingDirectory);

            var params = new LinkedHashMap<>(concordConfig.getParameters());
            params.put(runModeSettings.getFlowParameterName(), flowName);
            concordConfig.setParameters(params);
        } else {
            configName = flowName;
            concordConfig.setEntryPoint(flowName);
            concordConfig.setWorkingDirectory(workingDirectory);
        }

        concordConfig.setName(configName);
        return settings;
    }

    public static void createAndRun(
            @NotNull Project project,
            @NotNull String flowName,
            @NotNull String workingDirectory) {

        var runManager = RunManager.getInstance(project);
        var settings = createConfiguration(project, flowName, workingDirectory);

        runManager.setTemporaryConfiguration(settings);
        runManager.setSelectedConfiguration(settings);

        var executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
        if (executor != null) {
            ProgramRunnerUtil.executeConfiguration(settings, executor);
        }
    }

    public static @NotNull String getWorkingDirectory(
            @NotNull Project project,
            @NotNull VirtualFile virtualFile) {

        var scopes = ConcordScopeService.getInstance(project).getScopesForFile(virtualFile);
        if (!scopes.isEmpty()) {
            return scopes.getFirst().getRootDir().toString();
        }
        var parent = virtualFile.getParent();
        return parent != null ? parent.getPath() : "";
    }
}
