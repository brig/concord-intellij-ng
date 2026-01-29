package brig.concord.run;

import brig.concord.run.cli.ConcordCliManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ConcordCommandLineState extends CommandLineState {

    private final ConcordRunConfiguration myConfiguration;

    protected ConcordCommandLineState(@NotNull ExecutionEnvironment environment,
                                      @NotNull ConcordRunConfiguration configuration) {
        super(environment);
        myConfiguration = configuration;
    }

    @Override
    protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        var commandLine = createCommandLine();
        var processHandler = new KillableColoredProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler, getEnvironment().getProject());
        return processHandler;
    }

    @Override
    public @Nullable ConsoleView createConsole(@NotNull Executor executor) {
        var project = getEnvironment().getProject();
        var builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);

        var workingDirectory = myConfiguration.getWorkingDirectory();
        builder.addFilter(new ConcordErrorFilter(project, workingDirectory.isBlank() ? null : workingDirectory));

        return builder.getConsole();
    }

    private @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
        var cliManager = ConcordCliManager.getInstance();
        var cliPath = cliManager.getConfiguredCliPath();
        if (cliPath == null) {
            throw new ExecutionException("Concord CLI is not configured");
        }

        var commandLine = new PtyCommandLine();
        commandLine.setCharset(StandardCharsets.UTF_8);
        commandLine.setExePath(cliPath);
        commandLine.addParameter("run");

        var entryPoint = myConfiguration.getEntryPoint();
        if (!entryPoint.isBlank()) {
            commandLine.addParameter("--entry-point=" + entryPoint);
        }

        var parameters = myConfiguration.getParameters();
        for (var entry : parameters.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (!key.isBlank()) {
                commandLine.addParameter("-e");
                commandLine.addParameter(key + "=" + value);
            }
        }

        var additionalArgs = myConfiguration.getAdditionalArguments();
        if (!additionalArgs.isBlank()) {
            for (var arg : StringUtil.splitHonorQuotes(additionalArgs, ' ')) {
                var trimmed = arg.trim();
                if (!trimmed.isEmpty()) {
                    commandLine.addParameter(trimmed);
                }
            }
        }

        var workingDirectory = myConfiguration.getWorkingDirectory();
        if (!workingDirectory.isBlank()) {
            commandLine.setWorkDirectory(new File(workingDirectory));
        } else {
            var project = getEnvironment().getProject();
            var basePath = project.getBasePath();
            if (basePath != null) {
                commandLine.setWorkDirectory(new File(basePath));
            }
        }

        return commandLine;
    }
}
