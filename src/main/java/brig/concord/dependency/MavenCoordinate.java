// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.dependency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a Maven coordinate parsed from Concord dependency string.
 * Format: mvn://<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
 */
public final class MavenCoordinate {

    private static final Pattern COORDINATE_PATTERN =
            Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");

    private static final String DEFAULT_TYPE = "jar";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String type;

    private MavenCoordinate(@NotNull String groupId,
                            @NotNull String artifactId,
                            @NotNull String version,
                            @Nullable String classifier,
                            @NotNull String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.type = type;
    }

    /**
     * Parses a Concord dependency string.
     *
     * @param dependency string in format mvn://<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
     * @return parsed coordinate or null if format is invalid
     */
    public static @Nullable MavenCoordinate parse(@Nullable String dependency) {
        if (dependency == null || dependency.isBlank()) {
            return null;
        }
        if (dependency.startsWith("mvn://")) {
            dependency = dependency.substring("mvn://".length());
        } else {
            return null;
        }

        var matcher = COORDINATE_PATTERN.matcher(dependency.trim());
        if (!matcher.matches()) {
            return null;
        }
        var groupId = matcher.group(1);
        var artifactId = matcher.group(2);
        var type = get(matcher.group(4), DEFAULT_TYPE);
        var classifier = get(matcher.group(6), null);
        var version = matcher.group(7);

        if (containsPathTraversal(groupId) || containsPathTraversal(artifactId) || containsPathTraversal(version)
                || containsPathTraversal(type) || (classifier != null && containsPathTraversal(classifier))) {
            return null;
        }

        return new MavenCoordinate(groupId, artifactId, version, classifier, type);
    }

    /**
     * Returns the path within a Maven repository for this artifact.
     * Example: com/walmartlabs/concord/plugins/basic/http-tasks/2.35.0/http-tasks-2.35.0.jar
     */
    public @NotNull Path getRepositoryPath() {
        var groupPath = groupId.replace('.', '/');
        var fileName = buildFileName();
        return Path.of(groupPath, artifactId, version, fileName);
    }

    private @NotNull String buildFileName() {
        var sb = new StringBuilder();
        sb.append(artifactId).append("-").append(version);
        if (classifier != null && !classifier.isBlank()) {
            sb.append("-").append(classifier);
        }
        sb.append(".").append(type);
        return sb.toString();
    }

    public @NotNull String getGroupId() {
        return groupId;
    }

    public @NotNull String getArtifactId() {
        return artifactId;
    }

    public @NotNull String getVersion() {
        return version;
    }

    public @Nullable String getClassifier() {
        return classifier;
    }

    public @NotNull String getType() {
        return type;
    }

    public boolean isJar() {
        return "jar".equals(type);
    }

    /**
     * Returns true if the version is "latest".
     */
    public boolean isLatestVersion() {
        return "latest".equals(version);
    }

    /**
     * Returns true if the version contains at least one digit,
     * meaning it looks like a real version that Maven can resolve.
     */
    public boolean isResolvableVersion() {
        for (int i = 0; i < version.length(); i++) {
            if (Character.isDigit(version.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns groupId:artifactId (without version) for matching across versions.
     */
    public @NotNull String toGA() {
        return groupId + ":" + artifactId;
    }

    /**
     * Returns a new MavenCoordinate with a different version.
     */
    public @NotNull MavenCoordinate withVersion(@NotNull String newVersion) {
        return new MavenCoordinate(groupId, artifactId, newVersion, classifier, type);
    }

    /**
     * Returns the GAV string (groupId:artifactId:version).
     */
    public @NotNull String toGav() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenCoordinate that = (MavenCoordinate) o;
        return Objects.equals(groupId, that.groupId)
                && Objects.equals(artifactId, that.artifactId)
                && Objects.equals(version, that.version)
                && Objects.equals(classifier, that.classifier)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier, type);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("mvn://").append(groupId).append(":").append(artifactId);
        if (!DEFAULT_TYPE.equals(type) || classifier != null) {
            sb.append(":").append(type);
        }
        if (classifier != null) {
            sb.append(":").append(classifier);
        }
        sb.append(":").append(version);
        return sb.toString();
    }

    private static boolean containsPathTraversal(String value) {
        return value.contains("..") || value.startsWith("/");
    }

    private static String get(String value, String defaultValue) {
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }
}
