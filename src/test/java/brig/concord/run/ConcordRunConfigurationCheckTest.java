// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.run.cli.ConcordCliSettings;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConcordRunConfigurationCheckTest extends ConcordYamlTestBaseJunit5 {

    private ConcordRunConfiguration myConfiguration;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        var type = ConcordRunConfigurationType.getInstance();
        var configFactory = type.getConfigurationFactories()[0];
        myConfiguration = new ConcordRunConfiguration(getProject(), configFactory, "Test");
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        var settings = ConcordCliSettings.getInstance();
        settings.setCliPath(null);
        settings.setCliVersion(null);
        settings.setJdkName(null);

        var runModeSettings = ConcordRunModeSettings.getInstance(getProject());
        runModeSettings.setRunMode(ConcordRunModeSettings.RunMode.DIRECT);
        runModeSettings.setMainEntryPoint("default");
        runModeSettings.setFlowParameterName("flow");
        runModeSettings.setTargetDir(ConcordRunModeSettings.DEFAULT_TARGET_DIR);
        runModeSettings.setActiveProfiles(List.of());
        runModeSettings.setDefaultParameters(Map.of());

        myConfiguration = null;
        super.tearDown();
    }

    @Test
    void testEmptyEntryPoint_throwsError() {
        myConfiguration.setEntryPoint("");

        var error = assertThrows(RuntimeConfigurationError.class,
                () -> myConfiguration.checkConfiguration());
        assertEquals(ConcordBundle.message("run.configuration.entry.point.empty"), error.getMessage());
    }

    @Test
    void testCliNotConfigured_nullPath() {
        myConfiguration.setEntryPoint("myFlow");

        var settings = ConcordCliSettings.getInstance();
        settings.setCliPath(null);

        var error = assertThrows(RuntimeConfigurationError.class,
                () -> myConfiguration.checkConfiguration());
        assertEquals(ConcordBundle.message("run.configuration.cli.not.configured"), error.getMessage());
    }

    @Test
    void testCliNotConfigured_blankPath() {
        myConfiguration.setEntryPoint("myFlow");

        var settings = ConcordCliSettings.getInstance();
        settings.setCliPath("  ");

        var error = assertThrows(RuntimeConfigurationError.class,
                () -> myConfiguration.checkConfiguration());
        assertEquals(ConcordBundle.message("run.configuration.cli.not.configured"), error.getMessage());
    }

    @Test
    void testCliPathInvalid_nonexistentFile() {
        myConfiguration.setEntryPoint("myFlow");

        var settings = ConcordCliSettings.getInstance();
        settings.setCliPath("/nonexistent/path/to/cli");

        var error = assertThrows(RuntimeConfigurationError.class,
                () -> myConfiguration.checkConfiguration());
        assertEquals(ConcordBundle.message("run.configuration.cli.invalid"), error.getMessage());
    }

    @Test
    void testStaleJdk_throwsJdkNotFound() throws Exception {
        myConfiguration.setEntryPoint("myFlow");

        // Create a real executable file so CLI validation passes
        var tempFile = Files.createTempFile("cli", ".sh");
        try {
            Files.setPosixFilePermissions(tempFile, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            ));

            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(tempFile.toString());
            settings.setJdkName("nonexistent-jdk");

            var error = assertThrows(RuntimeConfigurationError.class,
                    () -> myConfiguration.checkConfiguration());
            assertEquals(ConcordBundle.message("run.configuration.jdk.not.found", "nonexistent-jdk"),
                    error.getMessage());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testMissingRequiredFlowParameter_throwsError() throws Exception {
        myConfiguration.setEntryPoint("deploy");
        myConfiguration.setWorkingDirectory(createFlowWithMandatoryInputParameter("deploy", "bucket"));

        var cliPath = createExecutableCli();
        try {
            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(cliPath.toString());
            settings.setJdkName(null);

            var error = assertThrows(RuntimeConfigurationError.class, () -> myConfiguration.checkConfiguration());
            assertEquals(
                    ConcordBundle.message("run.configuration.required.params.missing", "bucket"),
                    error.getMessage()
            );
        } finally {
            Files.deleteIfExists(cliPath);
        }
    }

    @Test
    void testRequiredFlowParameterProvidedInConfiguration_passes() throws Exception {
        myConfiguration.setEntryPoint("deploy");
        myConfiguration.setWorkingDirectory(createFlowWithMandatoryInputParameter("deploy", "bucket"));
        myConfiguration.setParameters(Map.of("bucket", "images-dev"));

        var cliPath = createExecutableCli();
        try {
            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(cliPath.toString());
            settings.setJdkName(null);

            assertDoesNotThrow(() -> myConfiguration.checkConfiguration());
        } finally {
            Files.deleteIfExists(cliPath);
        }
    }

    @Test
    void testRequiredFlowParameterProvidedByRunModeDefaults_passes() throws Exception {
        myConfiguration.setEntryPoint("deploy");
        myConfiguration.setWorkingDirectory(createFlowWithMandatoryInputParameter("deploy", "bucket"));

        var runModeSettings = ConcordRunModeSettings.getInstance(getProject());
        runModeSettings.setDefaultParameters(Map.of("bucket", "images-default"));

        var cliPath = createExecutableCli();
        try {
            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(cliPath.toString());
            settings.setJdkName(null);

            assertDoesNotThrow(() -> myConfiguration.checkConfiguration());
        } finally {
            Files.deleteIfExists(cliPath);
        }
    }

    @Test
    void testDelegatingMode_missingRequiredFlowParameter_throwsError() throws Exception {
        var runModeSettings = ConcordRunModeSettings.getInstance(getProject());
        runModeSettings.setRunMode(ConcordRunModeSettings.RunMode.DELEGATING);
        runModeSettings.setMainEntryPoint("default");
        runModeSettings.setFlowParameterName("flow");

        myConfiguration.setEntryPoint("default");
        myConfiguration.setWorkingDirectory(createFlowWithMandatoryInputParameter("deploy", "bucket"));
        myConfiguration.setParameters(Map.of("flow", "deploy"));

        var cliPath = createExecutableCli();
        try {
            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(cliPath.toString());
            settings.setJdkName(null);

            var error = assertThrows(RuntimeConfigurationError.class, () -> myConfiguration.checkConfiguration());
            assertEquals(
                    ConcordBundle.message("run.configuration.required.params.missing", "bucket"),
                    error.getMessage()
            );
        } finally {
            Files.deleteIfExists(cliPath);
        }
    }

    @Test
    void testDelegatingMode_requiredFlowParameterProvided_passes() throws Exception {
        var runModeSettings = ConcordRunModeSettings.getInstance(getProject());
        runModeSettings.setRunMode(ConcordRunModeSettings.RunMode.DELEGATING);
        runModeSettings.setMainEntryPoint("default");
        runModeSettings.setFlowParameterName("flow");

        myConfiguration.setEntryPoint("default");
        myConfiguration.setWorkingDirectory(createFlowWithMandatoryInputParameter("deploy", "bucket"));
        myConfiguration.setParameters(Map.of("flow", "deploy", "bucket", "images-dev"));

        var cliPath = createExecutableCli();
        try {
            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(cliPath.toString());
            settings.setJdkName(null);

            assertDoesNotThrow(() -> myConfiguration.checkConfiguration());
        } finally {
            Files.deleteIfExists(cliPath);
        }
    }

    private String createFlowWithMandatoryInputParameter(String flowName, String parameterName) {
        var file = createFile(
                "project-a/concord.yaml",
                """
                        flows:
                          ##
                          # in:
                          #   %s: string, mandatory
                          ##
                          %s:
                            - log: "ok"
                        """.formatted(parameterName, flowName)
        );

        var virtualFile = file.getVirtualFile();
        if (virtualFile == null || virtualFile.getParent() == null) {
            throw new AssertionError("Failed to resolve working directory for test file");
        }
        return virtualFile.getParent().getPath();
    }

    private static Path createExecutableCli() throws Exception {
        var tempFile = Files.createTempFile("concord-cli", ".sh");
        Files.setPosixFilePermissions(tempFile, EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE
        ));
        return tempFile;
    }
}
