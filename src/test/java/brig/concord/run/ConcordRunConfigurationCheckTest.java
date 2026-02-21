// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.run.cli.ConcordCliSettings;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        var tempFile = java.nio.file.Files.createTempFile("cli", ".sh");
        try {
            java.nio.file.Files.setPosixFilePermissions(tempFile, java.util.EnumSet.of(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                    java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                    java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
            ));

            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(tempFile.toString());
            settings.setJdkName("nonexistent-jdk");

            var error = assertThrows(RuntimeConfigurationError.class,
                    () -> myConfiguration.checkConfiguration());
            assertEquals(ConcordBundle.message("run.configuration.jdk.not.found", "nonexistent-jdk"),
                    error.getMessage());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }
}
