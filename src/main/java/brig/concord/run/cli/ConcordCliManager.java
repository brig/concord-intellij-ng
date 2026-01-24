package brig.concord.run.cli;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.io.HttpRequests;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

@Service(Service.Level.APP)
public final class ConcordCliManager {

    private static final Logger LOG = Logger.getInstance(ConcordCliManager.class);

    private static final String MAVEN_CENTRAL_BASE = "https://repo.maven.apache.org/maven2";
    private static final String GROUP_PATH = "com/walmartlabs/concord";
    private static final String ARTIFACT_ID = "concord-cli";
    private static final String METADATA_URL = MAVEN_CENTRAL_BASE + "/" + GROUP_PATH + "/" + ARTIFACT_ID + "/maven-metadata.xml";

    private static final Pattern VERSION_PATTERN = Pattern.compile("<version>([^<]+)</version>");

    public static @NotNull ConcordCliManager getInstance() {
        return ApplicationManager.getApplication().getService(ConcordCliManager.class);
    }

    public @NotNull List<String> fetchAvailableVersions() throws IOException {
        var metadata = HttpRequests.request(METADATA_URL)
                .accept("application/xml")
                .readString();

        List<String> versions = new ArrayList<>();
        var matcher = VERSION_PATTERN.matcher(metadata);
        while (matcher.find()) {
            versions.add(matcher.group(1));
        }

        versions.sort((v1, v2) -> compareVersions(parseVersion(v2), parseVersion(v1)));
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

    public boolean validateCliPath(@Nullable String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        var p = Path.of(path);
        return Files.isRegularFile(p) && Files.isExecutable(p);
    }

    public @Nullable String detectCliVersion(@NotNull String cliPath) {
        try {
            var pb = new ProcessBuilder(cliPath, "--version");
            pb.redirectErrorStream(true);
            var process = pb.start();

            var output = new String(process.getInputStream().readAllBytes()).trim();
            var exitCode = process.waitFor();

            if (exitCode == 0 && !output.isEmpty()) {
                return output.lines().findFirst().orElse(null);
            }
        } catch (Exception e) {
            LOG.warn("Failed to detect CLI version: " + e.getMessage());
        }
        return null;
    }

    public boolean isCliAvailable() {
        var settings = ConcordCliSettings.getInstance();
        return validateCliPath(settings.getCliPath());
    }

    public @Nullable String getConfiguredCliPath() {
        var settings = ConcordCliSettings.getInstance();
        var path = settings.getCliPath();
        return validateCliPath(path) ? path : null;
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

    private static int compareVersions(int @NotNull [] v1, int @NotNull [] v2) {
        var len = Math.max(v1.length, v2.length);
        for (var i = 0; i < len; i++) {
            var a = i < v1.length ? v1[i] : 0;
            var b = i < v2.length ? v2[i] : 0;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }

    private static int @NotNull [] parseVersion(@NotNull String version) {
        var parts = version.split("[.\\-]");
        var result = new int[parts.length];
        for (var i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }
}
