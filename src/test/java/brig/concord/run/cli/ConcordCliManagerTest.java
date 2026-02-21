// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.run.cli;

import com.intellij.testFramework.junit5.impl.TestApplicationExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestApplicationExtension.class)
class ConcordCliManagerTest {

    @Test
    void testValidateCliPath_nullPath() {
        var manager = ConcordCliManager.getInstance();
        assertFalse(manager.validateCliPath(null, null));
    }

    @Test
    void testValidateCliPath_blankPath() {
        var manager = ConcordCliManager.getInstance();
        assertFalse(manager.validateCliPath("", null));
        assertFalse(manager.validateCliPath("  ", null));
    }

    @Test
    void testValidateCliPath_nonexistentFile() {
        var manager = ConcordCliManager.getInstance();
        assertFalse(manager.validateCliPath("/nonexistent/path/to/cli", null));
    }

    @Test
    void testValidateCliPath_executableFile_noJdk(@TempDir Path tempDir) throws IOException {
        var file = createFile(tempDir, "cli.sh", true);

        var manager = ConcordCliManager.getInstance();
        assertTrue(manager.validateCliPath(file.toString(), null));
    }

    @Test
    void testValidateCliPath_nonExecutableFile_noJdk(@TempDir Path tempDir) throws IOException {
        var file = createFile(tempDir, "cli.jar", false);

        var manager = ConcordCliManager.getInstance();
        assertFalse(manager.validateCliPath(file.toString(), null));
    }

    @Test
    void testValidateCliPath_staleJdk_nonExecutableFile(@TempDir Path tempDir) throws IOException {
        var file = createFile(tempDir, "cli.jar", false);

        var manager = ConcordCliManager.getInstance();
        // JDK name is set but doesn't resolve (not in ProjectJdkTable).
        // Before the fix, this returned true (skipping executable check).
        // After the fix, it falls through to isExecutable and returns false.
        assertFalse(manager.validateCliPath(file.toString(), "nonexistent-jdk"));
    }

    @Test
    void testValidateCliPath_staleJdk_executableFile(@TempDir Path tempDir) throws IOException {
        var file = createFile(tempDir, "cli.sh", true);

        var manager = ConcordCliManager.getInstance();
        // JDK doesn't resolve, but file is executable -> should still pass
        assertTrue(manager.validateCliPath(file.toString(), "nonexistent-jdk"));
    }

    private static Path createFile(Path dir, String name, boolean executable) throws IOException {
        var file = dir.resolve(name);
        Files.writeString(file, "dummy");
        if (executable) {
            Files.setPosixFilePermissions(file, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            ));
        } else {
            Files.setPosixFilePermissions(file, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
            ));
        }
        return file;
    }
}