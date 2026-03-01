// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Data model matching the Concord CLI {@code ~/.concord/mvn.json} structure.
 */
final class MvnJsonConfig {

    public static Repository mavenCentral() {
        var repo = new Repository("central", "https://repo.maven.apache.org/maven2/");
        repo.setSnapshotPolicy(new PolicyConfig(false, null, null));
        return repo;
    }

    private @Nullable List<Repository> repositories;

    public MvnJsonConfig() {
    }

    public MvnJsonConfig(@Nullable List<Repository> repositories) {
        this.repositories = repositories;
    }

    public @NotNull List<Repository> getRepositories() {
        return repositories != null ? repositories : List.of();
    }

    public void setRepositories(@Nullable List<Repository> repositories) {
        this.repositories = repositories;
    }

    public static final class Repository {

        private @Nullable String id;
        private @Nullable String url;
        private @Nullable AuthConfig auth;
        private @Nullable PolicyConfig snapshotPolicy;
        private @Nullable PolicyConfig releasePolicy;
        private @Nullable ProxyConfig proxy;

        public Repository() {
        }

        public Repository(@Nullable String id, @Nullable String url) {
            this.id = id;
            this.url = url;
        }

        public @Nullable String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        public @Nullable String getUrl() {
            return url;
        }

        public void setUrl(@Nullable String url) {
            this.url = url;
        }

        public @Nullable AuthConfig getAuth() {
            return auth;
        }

        public void setAuth(@Nullable AuthConfig auth) {
            this.auth = auth;
        }

        public @Nullable PolicyConfig getSnapshotPolicy() {
            return snapshotPolicy;
        }

        public void setSnapshotPolicy(@Nullable PolicyConfig snapshotPolicy) {
            this.snapshotPolicy = snapshotPolicy;
        }

        public @Nullable PolicyConfig getReleasePolicy() {
            return releasePolicy;
        }

        public void setReleasePolicy(@Nullable PolicyConfig releasePolicy) {
            this.releasePolicy = releasePolicy;
        }

        public @Nullable ProxyConfig getProxy() {
            return proxy;
        }

        public void setProxy(@Nullable ProxyConfig proxy) {
            this.proxy = proxy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Repository that)) {
                return false;
            }
            return Objects.equals(id, that.id)
                    && Objects.equals(url, that.url)
                    && Objects.equals(auth, that.auth)
                    && Objects.equals(snapshotPolicy, that.snapshotPolicy)
                    && Objects.equals(releasePolicy, that.releasePolicy)
                    && Objects.equals(proxy, that.proxy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, url, auth, snapshotPolicy, releasePolicy, proxy);
        }

        public Repository copy() {
            var copy = new Repository(id, url);
            if (auth != null) {
                copy.auth = new AuthConfig(auth.username, auth.password);
            }
            if (snapshotPolicy != null) {
                copy.snapshotPolicy = new PolicyConfig(snapshotPolicy.enabled, snapshotPolicy.updatePolicy, snapshotPolicy.checksumPolicy);
            }
            if (releasePolicy != null) {
                copy.releasePolicy = new PolicyConfig(releasePolicy.enabled, releasePolicy.updatePolicy, releasePolicy.checksumPolicy);
            }
            if (proxy != null) {
                copy.proxy = new ProxyConfig(proxy.type, proxy.host, proxy.port);
            }
            return copy;
        }
    }

    public static final class AuthConfig {

        private @Nullable String username;
        private @Nullable String password;

        public AuthConfig() {
        }

        public AuthConfig(@Nullable String username, @Nullable String password) {
            this.username = username;
            this.password = password;
        }

        public @Nullable String getUsername() {
            return username;
        }

        public void setUsername(@Nullable String username) {
            this.username = username;
        }

        public @Nullable String getPassword() {
            return password;
        }

        public void setPassword(@Nullable String password) {
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AuthConfig that)) {
                return false;
            }
            return Objects.equals(username, that.username)
                    && Objects.equals(password, that.password);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, password);
        }
    }

    public static final class PolicyConfig {

        private @Nullable Boolean enabled;
        private @Nullable String updatePolicy;
        private @Nullable String checksumPolicy;

        public PolicyConfig() {
        }

        public PolicyConfig(@Nullable Boolean enabled, @Nullable String updatePolicy, @Nullable String checksumPolicy) {
            this.enabled = enabled;
            this.updatePolicy = updatePolicy;
            this.checksumPolicy = checksumPolicy;
        }

        public boolean isEnabled() {
            return enabled == null || enabled;
        }

        public void setEnabled(@Nullable Boolean enabled) {
            this.enabled = enabled;
        }

        public String getUpdatePolicy() {
            return updatePolicy != null ? updatePolicy : "never";
        }

        public void setUpdatePolicy(@Nullable String updatePolicy) {
            this.updatePolicy = updatePolicy;
        }

        public String getChecksumPolicy() {
            return checksumPolicy != null ? checksumPolicy : "ignore";
        }

        public void setChecksumPolicy(@Nullable String checksumPolicy) {
            this.checksumPolicy = checksumPolicy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PolicyConfig that)) {
                return false;
            }
            return isEnabled() == that.isEnabled()
                    && Objects.equals(getUpdatePolicy(), that.getUpdatePolicy())
                    && Objects.equals(getChecksumPolicy(), that.getChecksumPolicy());
        }

        @Override
        public int hashCode() {
            return Objects.hash(isEnabled(), getUpdatePolicy(), getChecksumPolicy());
        }
    }

    public static final class ProxyConfig {

        private @Nullable String type;
        private @Nullable String host;
        private @Nullable Integer port;

        public ProxyConfig() {
        }

        public ProxyConfig(@Nullable String type, @Nullable String host, @Nullable Integer port) {
            this.type = type;
            this.host = host;
            this.port = port;
        }

        public String getType() {
            return type != null ? type : "http";
        }

        public void setType(@Nullable String type) {
            this.type = type;
        }

        public @Nullable String getHost() {
            return host;
        }

        public void setHost(@Nullable String host) {
            this.host = host;
        }

        public @Nullable Integer getPort() {
            return port;
        }

        public void setPort(@Nullable Integer port) {
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ProxyConfig that)) {
                return false;
            }
            return Objects.equals(getType(), that.getType())
                    && Objects.equals(host, that.host)
                    && Objects.equals(port, that.port);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getType(), host, port);
        }
    }
}
