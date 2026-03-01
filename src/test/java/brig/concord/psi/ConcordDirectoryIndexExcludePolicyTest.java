// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.run.ConcordRunModeSettings;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConcordDirectoryIndexExcludePolicyTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void defaultTargetDir() {
        var policy = new ConcordDirectoryIndexExcludePolicy(getProject());
        var urls = policy.getExcludeUrlsForProject();

        var basePath = getProject().getBasePath();
        assertNotNull(basePath);

        var expected = VfsUtilCore.pathToUrl(
                Path.of(basePath).resolve(ConcordRunModeSettings.DEFAULT_TARGET_DIR).toString());
        assertArrayEquals(new String[]{expected}, urls);
    }

    @Test
    void customTargetDir() {
        var settings = ConcordRunModeSettings.getInstance(getProject());
        settings.setTargetDir("build/output");

        var policy = new ConcordDirectoryIndexExcludePolicy(getProject());
        var urls = policy.getExcludeUrlsForProject();

        var basePath = getProject().getBasePath();
        assertNotNull(basePath);

        var expected = VfsUtilCore.pathToUrl(
                Path.of(basePath).resolve("build/output").toString());
        assertArrayEquals(new String[]{expected}, urls);
    }

    @Test
    void blankTargetDir_returnsEmpty() {
        var settings = ConcordRunModeSettings.getInstance(getProject());
        settings.setTargetDir("");

        var policy = new ConcordDirectoryIndexExcludePolicy(getProject());
        var urls = policy.getExcludeUrlsForProject();

        assertEquals(0, urls.length);
    }

    @Test
    void absoluteTargetDir_usedAsIs() {
        var settings = ConcordRunModeSettings.getInstance(getProject());
        settings.setTargetDir("/tmp/concord-target");

        var policy = new ConcordDirectoryIndexExcludePolicy(getProject());
        var urls = policy.getExcludeUrlsForProject();

        var expected = VfsUtilCore.pathToUrl("/tmp/concord-target");
        assertArrayEquals(new String[]{expected}, urls);
    }
}