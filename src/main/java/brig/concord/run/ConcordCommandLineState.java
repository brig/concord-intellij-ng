package brig.concord.run;

import brig.concord.run.cli.ConcordCliManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConcordCommandLineState extends CommandLineState {

    private final ConcordRunConfiguration myConfiguration;

    protected ConcordCommandLineState(@NotNull ExecutionEnvironment environment,
                                      @NotNull ConcordRunConfiguration configuration) {
        super(environment);
        myConfiguration = configuration;
    }

    @Override
    protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine();
        KillableColoredProcessHandler processHandler = new KillableColoredProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
        return processHandler;
    }

    @Override
    public @Nullable ConsoleView createConsole(@NotNull Executor executor) {
        Project project = getEnvironment().getProject();
        TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);

        String workingDirectory = myConfiguration.getWorkingDirectory();
        builder.addFilter(new ConcordErrorFilter(project, workingDirectory.isBlank() ? null : workingDirectory));

        return builder.getConsole();
    }

    private @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
        ConcordCliManager cliManager = ConcordCliManager.getInstance();
        String cliPath = cliManager.getConfiguredCliPath();
        if (cliPath == null) {
            throw new ExecutionException("Concord CLI is not configured");
        }

        PtyCommandLine commandLine = new PtyCommandLine();
        commandLine.setCharset(StandardCharsets.UTF_8);
        commandLine.setExePath(cliPath);
        commandLine.addParameter("run");

        String entryPoint = myConfiguration.getEntryPoint();
        if (!entryPoint.isBlank()) {
            commandLine.addParameter("--entry-point=" + entryPoint);
        }

        Map<String, String> parameters = myConfiguration.getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.isBlank()) {
                commandLine.addParameter("-e");
                commandLine.addParameter(key + "=" + value);
            }
        }

        String additionalArgs = myConfiguration.getAdditionalArguments();
        if (!additionalArgs.isBlank()) {
            for (String arg : StringUtil.splitHonorQuotes(additionalArgs, ' ')) {
                String trimmed = arg.trim();
                if (!trimmed.isEmpty()) {
                    commandLine.addParameter(trimmed);
                }
            }
        }

        String workingDirectory = myConfiguration.getWorkingDirectory();
        if (!workingDirectory.isBlank()) {
            commandLine.setWorkDirectory(new File(workingDirectory));
        } else {
            Project project = getEnvironment().getProject();
            String basePath = project.getBasePath();
            if (basePath != null) {
                commandLine.setWorkDirectory(new File(basePath));
            }
        }

        return commandLine;
    }
}
