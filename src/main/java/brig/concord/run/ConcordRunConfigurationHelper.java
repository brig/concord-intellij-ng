// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
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
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;

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

        var existing = findExistingConfiguration(project, flowName, workingDirectory);
        var settings = existing != null ? existing : createConfiguration(project, flowName, workingDirectory);

        if (existing == null) {
            runManager.setTemporaryConfiguration(settings);
        }
        runManager.setSelectedConfiguration(settings);

        var executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
        if (executor != null) {
            ProgramRunnerUtil.executeConfiguration(settings, executor);
        }
    }

    public static boolean isMatchingConfiguration(
            @NotNull ConcordRunConfiguration config,
            @NotNull String flowName,
            @NotNull String workingDirectory,
            @NotNull Project project) {

        if (!workingDirectory.equals(config.getWorkingDirectory())) {
            return false;
        }

        var runModeSettings = ConcordRunModeSettings.getInstance(project);
        if (runModeSettings.isDelegatingMode()) {
            if (!runModeSettings.getMainEntryPoint().equals(config.getEntryPoint())) {
                return false;
            }
            var flowParamValue = config.getParameters().get(runModeSettings.getFlowParameterName());
            return Objects.equals(flowName, flowParamValue);
        } else {
            return flowName.equals(config.getEntryPoint());
        }
    }

    private static @Nullable RunnerAndConfigurationSettings findExistingConfiguration(
            @NotNull Project project,
            @NotNull String flowName,
            @NotNull String workingDirectory) {

        var runManager = RunManager.getInstance(project);
        var configurationType = ConcordRunConfigurationType.getInstance();

        for (var settings : runManager.getConfigurationSettingsList(configurationType)) {
            if (settings.getConfiguration() instanceof ConcordRunConfiguration config
                    && isMatchingConfiguration(config, flowName, workingDirectory, project)) {
                return settings;
            }
        }

        return null;
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
