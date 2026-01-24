package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.run.cli.ConcordCliManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConcordRunConfiguration extends RunConfigurationBase<ConcordRunConfigurationOptions> {

    protected ConcordRunConfiguration(@NotNull Project project,
                                      @NotNull ConfigurationFactory factory,
                                      @NotNull String name) {
        super(project, factory, name);
    }

    @Override
    protected @NotNull ConcordRunConfigurationOptions getOptions() {
        return (ConcordRunConfigurationOptions) super.getOptions();
    }

    public @NotNull String getEntryPoint() {
        return getOptions().getEntryPoint();
    }

    public void setEntryPoint(@NotNull String entryPoint) {
        getOptions().setEntryPoint(entryPoint);
    }

    public @NotNull String getWorkingDirectory() {
        return getOptions().getWorkingDirectory();
    }

    public void setWorkingDirectory(@NotNull String workingDirectory) {
        getOptions().setWorkingDirectory(workingDirectory);
    }

    public @NotNull String getAdditionalArguments() {
        return getOptions().getAdditionalArguments();
    }

    public void setAdditionalArguments(@NotNull String additionalArguments) {
        getOptions().setAdditionalArguments(additionalArguments);
    }

    public @NotNull Map<String, String> getParameters() {
        return getOptions().getParameters();
    }

    public void setParameters(@NotNull Map<String, String> parameters) {
        getOptions().setParameters(parameters);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new ConcordRunConfigurationEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        String entryPoint = getEntryPoint();
        if (entryPoint.isBlank()) {
            throw new RuntimeConfigurationError(ConcordBundle.message("run.configuration.entry.point.empty"));
        }

        ConcordCliManager cliManager = ConcordCliManager.getInstance();
        if (!cliManager.isCliAvailable()) {
            String cliPath = cliManager.getConfiguredCliPath();
            if (cliPath == null) {
                throw new RuntimeConfigurationError(ConcordBundle.message("run.configuration.cli.not.configured"));
            } else {
                throw new RuntimeConfigurationError(ConcordBundle.message("run.configuration.cli.invalid"));
            }
        }
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor,
                                              @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new ConcordCommandLineState(environment, this);
    }
}
