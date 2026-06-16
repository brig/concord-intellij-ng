// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.dependency.ConcordRepositorySettings;
import brig.concord.run.cli.ConcordCliManager;
import brig.concord.run.cli.ConcordCliSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConcordCommandLineState extends CommandLineState {

    private static final Logger LOG = Logger.getInstance(ConcordCommandLineState.class);
    private static final DateTimeFormatter LOG_FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Pattern UNSAFE_FILE_NAME_CHARS = Pattern.compile("[^a-z0-9._-]");
    private static final Pattern TRAILING_FILE_NAME_SEPARATORS = Pattern.compile("[._-]+$");

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
        attachOutputFileWriter(processHandler);
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

    private static final String TARGET_DIR_MIN_VERSION = "2.37.0";

    private static boolean cliSupportsTargetDir() {
        var version = ConcordCliSettings.getInstance().getCliVersion();
        if (version == null || version.isBlank()) {
            return false;
        }
        return VersionComparatorUtil.compare(version, TARGET_DIR_MIN_VERSION) >= 0;
    }

    private @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
        var cliManager = ConcordCliManager.getInstance();
        var cliPath = cliManager.getConfiguredCliPath();
        if (cliPath == null) {
            throw new ExecutionException(ConcordBundle.message("run.configuration.cli.not.configured"));
        }

        var project = getEnvironment().getProject();
        var commandLine = new PtyCommandLine();
        commandLine.setCharset(StandardCharsets.UTF_8);

        var jdkName = ConcordCliSettings.getInstance().getJdkName();
        if (jdkName != null) {
            var jdkInfo = cliManager.resolveJdk(jdkName);
            if (jdkInfo == null) {
                throw new ExecutionException(ConcordBundle.message("run.configuration.jdk.not.found", jdkName));
            }
            commandLine.setExePath(jdkInfo.javaPath());
            commandLine.addParameter("-jar");
            commandLine.addParameter(cliPath);
            commandLine.withEnvironment("JAVA_HOME", jdkInfo.homePath());
        } else {
            commandLine.setExePath(cliPath);
        }

        commandLine.addParameter("run");

        var depsCachePath = ConcordRepositorySettings.getInstance().getEffectiveDepsCachePath();
        commandLine.addParameter("--deps-cache-dir=" + depsCachePath);

        var runModeSettings = ConcordRunModeSettings.getInstance(project);

        var targetDir = runModeSettings.getTargetDir();
        if (!targetDir.isBlank() && cliSupportsTargetDir()) {
            var targetPath = Path.of(targetDir);
            if (targetPath.isAbsolute()) {
                commandLine.addParameter("--target-dir=" + targetDir);
            } else {
                var basePath = project.getBasePath();
                if (basePath != null) {
                    commandLine.addParameter("--target-dir=" + Path.of(basePath).resolve(targetDir));
                }
            }
        }

        // Build parameters using helper
        var buildResult = ConcordCommandLineBuilder.buildParameters(
                myConfiguration.getEntryPoint(),
                myConfiguration.getParameters(),
                runModeSettings.isDelegatingMode(),
                runModeSettings.getMainEntryPoint(),
                runModeSettings.getFlowParameterName(),
                runModeSettings.getDefaultParameters(),
                runModeSettings.getActiveProfiles()
        );

        // Add entry point
        var entryPoint = buildResult.entryPoint();
        if (entryPoint != null) {
            commandLine.addParameter("--entry-point=" + entryPoint);
        }

        // Add parameters
        for (var entry : buildResult.parameters().entrySet()) {
            if (!entry.getKey().isBlank()) {
                commandLine.addParameter("-e");
                commandLine.addParameter(entry.getKey() + "=" + entry.getValue());
            }
        }

        // Add profiles
        for (var profile : buildResult.profiles()) {
            if (!profile.isBlank()) {
                commandLine.addParameter("-p");
                commandLine.addParameter(profile);
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
            var basePath = project.getBasePath();
            if (basePath != null) {
                commandLine.setWorkDirectory(new File(basePath));
            }
        }

        return commandLine;
    }

    private void attachOutputFileWriter(@NotNull ProcessHandler processHandler) throws ExecutionException {
        if (!myConfiguration.isSaveOutputToFile()) {
            return;
        }

        var outputFile = resolveOutputFile();
        try {
            var parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            var writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
            var outputFileLock = new Object();
            processHandler.addProcessListener(new ProcessAdapter() {
                private volatile boolean loggingFailed;

                @Override
                public void startNotified(@NotNull ProcessEvent event) {
                    processHandler.notifyTextAvailable(
                            ConcordBundle.message("run.configuration.output.file.console.message", outputFile) + "\n",
                            ProcessOutputTypes.SYSTEM
                    );
                }

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    if (loggingFailed) {
                        return;
                    }

                    var text = event.getText();
                    if (text.isEmpty()) {
                        return;
                    }

                    synchronized (outputFileLock) {
                        try {
                            writer.write(text);
                            if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
                                writer.flush();
                            }
                        } catch (IOException e) {
                            loggingFailed = true;
                            LOG.warn("Failed to write Concord run output to " + outputFile, e);
                        }
                    }
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    synchronized (outputFileLock) {
                        try {
                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            LOG.warn("Failed to close Concord run output file " + outputFile, e);
                        }
                    }
                }
            });
        } catch (IOException e) {
            throw new ExecutionException(ConcordBundle.message("run.configuration.output.file.error", outputFile), e);
        } catch (InvalidPathException e) {
            throw new ExecutionException(ConcordBundle.message("run.configuration.output.file.invalid.path", e.getInput()), e);
        }
    }

    private @NotNull Path resolveOutputFile() throws ExecutionException {
        var configuredOutputFile = myConfiguration.getOutputFile();
        try {
            if (!configuredOutputFile.isBlank()) {
                var outputPath = Path.of(configuredOutputFile);
                var resolvedPath = outputPath.isAbsolute() ? outputPath : getBaseDirectory().resolve(outputPath);
                return Files.isDirectory(resolvedPath) ? resolvedPath.resolve(createDefaultLogFileName()) : resolvedPath;
            }

            return getBaseDirectory()
                    .resolve(".concord")
                    .resolve("run-logs")
                    .resolve(createDefaultLogFileName());
        } catch (InvalidPathException e) {
            throw new ExecutionException(ConcordBundle.message("run.configuration.output.file.invalid.path", e.getInput()), e);
        }
    }

    private @NotNull Path getBaseDirectory() {
        var workingDirectory = myConfiguration.getWorkingDirectory();
        if (!workingDirectory.isBlank()) {
            return Path.of(workingDirectory);
        }

        var basePath = getEnvironment().getProject().getBasePath();
        if (basePath != null) {
            return Path.of(basePath);
        }

        return Path.of(System.getProperty("user.home"));
    }

    private static @NotNull String sanitizeFileName(@NotNull String fileName) {
        var lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
        var result = UNSAFE_FILE_NAME_CHARS.matcher(lowerCaseFileName).replaceAll("_");
        result = TRAILING_FILE_NAME_SEPARATORS.matcher(result).replaceAll("");
        return result.isBlank() ? "concord-run" : result;
    }

    private @NotNull String createDefaultLogFileName() {
        return sanitizeFileName(myConfiguration.getName()) + "-" + LOG_FILE_TIMESTAMP.format(LocalDateTime.now()) + ".log";
    }
}
