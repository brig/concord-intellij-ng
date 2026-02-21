// SPDX-License-Identifier: Apache-2.0
package brig.concord.run.cli;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.InputSource;


import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service(Service.Level.APP)
public final class ConcordCliManager {

    private static final Logger LOG = Logger.getInstance(ConcordCliManager.class);

    private static final String MAVEN_CENTRAL_BASE = "https://repo.maven.apache.org/maven2";
    private static final String GROUP_PATH = "com/walmartlabs/concord";
    private static final String ARTIFACT_ID = "concord-cli";
    private static final String METADATA_URL = MAVEN_CENTRAL_BASE + "/" + GROUP_PATH + "/" + ARTIFACT_ID + "/maven-metadata.xml";

    public record JdkInfo(@NotNull String javaPath, @NotNull String homePath) {}

    public static @NotNull ConcordCliManager getInstance() {
        return ApplicationManager.getApplication().getService(ConcordCliManager.class);
    }

    public @Nullable JdkInfo resolveJdk(@Nullable String jdkName) {
        if (jdkName == null) {
            return null;
        }

        var jdk = ProjectJdkTable.getInstance().findJdk(jdkName);
        if (jdk == null) {
            LOG.warn("Configured JDK '" + jdkName + "' not found, falling back to default");
            return null;
        }

        var homePath = jdk.getHomePath();
        if (homePath == null) {
            LOG.warn("JDK '" + jdkName + "' has no home path, falling back to default");
            return null;
        }

        var javaExe = SystemInfo.isWindows ? "java.exe" : "java";
        var javaPath = Path.of(homePath, "bin", javaExe);
        if (!Files.isRegularFile(javaPath)) {
            LOG.warn("Java executable not found at " + javaPath + ", falling back to default");
            return null;
        }

        return new JdkInfo(javaPath.toString(), homePath);
    }

    public @NotNull List<String> fetchAvailableVersions() throws IOException {
        var metadata = HttpRequests.request(METADATA_URL)
                .accept("application/xml")
                .readString();

        List<String> versions = new ArrayList<>();
        try {
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            var inputSource = new InputSource(new StringReader(metadata));
            var doc = builder.parse(inputSource);
            var versionNodes = doc.getElementsByTagName("version");
            for (int i = 0; i < versionNodes.getLength(); i++) {
                versions.add(versionNodes.item(i).getTextContent());
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse maven-metadata.xml", e);
        }

        versions.sort(VersionComparatorUtil.COMPARATOR.reversed());
        return versions;
    }

    public void downloadCli(@NotNull String version, @NotNull ProgressIndicator indicator) throws IOException {
        var downloadUrl = buildDownloadUrl(version);
        var targetPath = getCliPathForVersion(version);

        indicator.setText("Downloading Concord CLI " + version + "...");
        indicator.setIndeterminate(false);

        var parentDir = targetPath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }

        var targetFile = targetPath.toFile();
        HttpRequests.request(downloadUrl)
                .productNameAsUserAgent()
                .saveToFile(targetFile, indicator);

        if (!SystemInfo.isWindows) {
            makeExecutable(targetPath);
        }

        var settings = ConcordCliSettings.getInstance();
        settings.setCliPath(targetPath.toString());
        settings.setCliVersion(version);
    }

    public boolean validateCliPath(@Nullable String path, @Nullable String jdkName) {
        if (path == null || path.isBlank()) {
            return false;
        }

        var p = Path.of(path);
        if (!Files.isRegularFile(p)) {
            return false;
        }

        // When a JDK is configured and resolves, the CLI runs via "java -jar" so it only needs to be a regular file
        if (jdkName != null && resolveJdk(jdkName) != null) {
            return true;
        }

        return Files.isExecutable(p);
    }

    public @Nullable String detectCliVersion(@NotNull String cliPath, @Nullable JdkInfo jdkInfo) {
        Process process = null;
        try {
            ProcessBuilder pb;
            if (jdkInfo != null) {
                pb = new ProcessBuilder(jdkInfo.javaPath(), "-jar", cliPath, "--version");
                pb.environment().put("JAVA_HOME", jdkInfo.homePath());
            } else {
                pb = new ProcessBuilder(cliPath, "--version");
            }
            pb.redirectErrorStream(true);
            process = pb.start();

            var completed = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                LOG.warn("CLI version detection timed out");
                return null;
            }

            var output = new String(process.getInputStream().readAllBytes()).trim();
            var exitCode = process.exitValue();

            if (exitCode == 0 && !output.isEmpty()) {
                return output.lines().findFirst().orElse(null);
            }
        } catch (Exception e) {
            LOG.warn("Failed to detect CLI version: " + e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
        return null;
    }

    public boolean isCliAvailable() {
        var settings = ConcordCliSettings.getInstance();
        return validateCliPath(settings.getCliPath(), settings.getJdkName());
    }

    public @Nullable String getConfiguredCliPath() {
        var settings = ConcordCliSettings.getInstance();
        var path = settings.getCliPath();
        return validateCliPath(path, settings.getJdkName()) ? path : null;
    }

    private @NotNull Path getCliPathForVersion(@NotNull String version) {
        var extension = SystemInfo.isWindows ? "-executable.jar" : ".sh";
        var fileName = ARTIFACT_ID + "-" + version + extension;
        return Path.of(PathManager.getSystemPath(), "concord-cli").resolve(fileName);
    }

    private @NotNull String buildDownloadUrl(@NotNull String version) {
        var extension = SystemInfo.isWindows ? "-executable.jar" : ".sh";
        return MAVEN_CENTRAL_BASE + "/" + GROUP_PATH + "/" + ARTIFACT_ID + "/" + version + "/" + ARTIFACT_ID + "-" + version + extension;
    }

    private static void makeExecutable(@NotNull Path path) throws IOException {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE
            ));
        } catch (UnsupportedOperationException e) {
            LOG.debug("POSIX permissions not supported on this system");
        }
    }
}
