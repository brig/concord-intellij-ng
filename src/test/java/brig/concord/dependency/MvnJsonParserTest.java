// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MvnJsonParserTest {

    @Test
    void parseFullConfig(@TempDir Path tempDir) throws IOException {
        var json = """
                {
                  "repositories": [
                    {
                      "id": "central",
                      "url": "https://repo.maven.apache.org/maven2",
                      "auth": {
                        "username": "user1",
                        "password": "pass1"
                      },
                      "snapshotPolicy": {
                        "enabled": false,
                        "updatePolicy": "always",
                        "checksumPolicy": "fail"
                      },
                      "releasePolicy": {
                        "enabled": true,
                        "updatePolicy": "daily",
                        "checksumPolicy": "warn"
                      },
                      "proxy": {
                        "type": "https",
                        "host": "proxy.example.com",
                        "port": 8080
                      }
                    }
                  ]
                }
                """;

        var path = tempDir.resolve("mvn.json");
        Files.writeString(path, json);

        var config = MvnJsonParser.read(path);

        assertThat(config.getRepositories()).hasSize(1);

        var repo = config.getRepositories().get(0);
        assertThat(repo.getId()).isEqualTo("central");
        assertThat(repo.getUrl()).isEqualTo("https://repo.maven.apache.org/maven2");

        assertThat(repo.getAuth()).isNotNull();
        assertThat(repo.getAuth().getUsername()).isEqualTo("user1");
        assertThat(repo.getAuth().getPassword()).isEqualTo("pass1");

        assertThat(repo.getSnapshotPolicy()).isNotNull();
        assertThat(repo.getSnapshotPolicy().isEnabled()).isFalse();
        assertThat(repo.getSnapshotPolicy().getUpdatePolicy()).isEqualTo("always");
        assertThat(repo.getSnapshotPolicy().getChecksumPolicy()).isEqualTo("fail");

        assertThat(repo.getReleasePolicy()).isNotNull();
        assertThat(repo.getReleasePolicy().isEnabled()).isTrue();
        assertThat(repo.getReleasePolicy().getUpdatePolicy()).isEqualTo("daily");
        assertThat(repo.getReleasePolicy().getChecksumPolicy()).isEqualTo("warn");

        assertThat(repo.getProxy()).isNotNull();
        assertThat(repo.getProxy().getType()).isEqualTo("https");
        assertThat(repo.getProxy().getHost()).isEqualTo("proxy.example.com");
        assertThat(repo.getProxy().getPort()).isEqualTo(8080);
    }

    @Test
    void parseMinimalConfig(@TempDir Path tempDir) throws IOException {
        var json = """
                {
                  "repositories": [
                    {
                      "id": "my-repo",
                      "url": "https://my.repo.com/maven2"
                    }
                  ]
                }
                """;

        var path = tempDir.resolve("mvn.json");
        Files.writeString(path, json);

        var config = MvnJsonParser.read(path);

        assertThat(config.getRepositories()).hasSize(1);

        var repo = config.getRepositories().get(0);
        assertThat(repo.getId()).isEqualTo("my-repo");
        assertThat(repo.getUrl()).isEqualTo("https://my.repo.com/maven2");
        assertThat(repo.getAuth()).isNull();
        assertThat(repo.getSnapshotPolicy()).isNull();
        assertThat(repo.getReleasePolicy()).isNull();
        assertThat(repo.getProxy()).isNull();
    }

    @Test
    void writeAndReReadRoundtrip(@TempDir Path tempDir) throws IOException {
        var config = new MvnJsonConfig();
        var repo = new MvnJsonConfig.Repository("test-repo", "https://test.example.com/repo");
        repo.setAuth(new MvnJsonConfig.AuthConfig("testuser", "testpass"));
        repo.setReleasePolicy(new MvnJsonConfig.PolicyConfig(true, "daily", "warn"));
        repo.setSnapshotPolicy(new MvnJsonConfig.PolicyConfig(false, "never", "ignore"));
        repo.setProxy(new MvnJsonConfig.ProxyConfig("http", "proxy.test.com", 3128));
        config.setRepositories(java.util.List.of(repo));

        var path = tempDir.resolve("mvn.json");
        MvnJsonParser.write(path, config);

        assertThat(path).exists();

        var reRead = MvnJsonParser.read(path);

        assertThat(reRead.getRepositories()).hasSize(1);
        var reReadRepo = reRead.getRepositories().get(0);
        assertThat(reReadRepo.getId()).isEqualTo("test-repo");
        assertThat(reReadRepo.getUrl()).isEqualTo("https://test.example.com/repo");
        assertThat(reReadRepo.getAuth().getUsername()).isEqualTo("testuser");
        assertThat(reReadRepo.getAuth().getPassword()).isEqualTo("testpass");
        assertThat(reReadRepo.getReleasePolicy().isEnabled()).isTrue();
        assertThat(reReadRepo.getReleasePolicy().getUpdatePolicy()).isEqualTo("daily");
        assertThat(reReadRepo.getReleasePolicy().getChecksumPolicy()).isEqualTo("warn");
        assertThat(reReadRepo.getSnapshotPolicy().isEnabled()).isFalse();
        assertThat(reReadRepo.getSnapshotPolicy().getUpdatePolicy()).isEqualTo("never");
        assertThat(reReadRepo.getSnapshotPolicy().getChecksumPolicy()).isEqualTo("ignore");
        assertThat(reReadRepo.getProxy().getType()).isEqualTo("http");
        assertThat(reReadRepo.getProxy().getHost()).isEqualTo("proxy.test.com");
        assertThat(reReadRepo.getProxy().getPort()).isEqualTo(3128);
    }

    @Test
    void missingFileReturnsEmptyConfig(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("nonexistent.json");

        var config = MvnJsonParser.read(path);

        assertThat(config).isNotNull();
        assertThat(config.getRepositories()).isEmpty();
    }

    @Test
    void malformedJsonThrowsException(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("mvn.json");
        Files.writeString(path, "{ this is not valid json }}}");

        assertThatThrownBy(() -> MvnJsonParser.read(path))
                .isInstanceOf(JsonSyntaxException.class);
    }

    @Test
    void policyDefaults() {
        var policy = new MvnJsonConfig.PolicyConfig();

        assertThat(policy.isEnabled()).isTrue();
        assertThat(policy.getUpdatePolicy()).isEqualTo("never");
        assertThat(policy.getChecksumPolicy()).isEqualTo("ignore");
    }

    @Test
    void proxyDefaults() {
        var proxy = new MvnJsonConfig.ProxyConfig();

        assertThat(proxy.getType()).isEqualTo("http");
        assertThat(proxy.getHost()).isNull();
        assertThat(proxy.getPort()).isNull();
    }

    @Test
    void writeCreatesParentDirectories(@TempDir Path tempDir) throws IOException {
        var path = tempDir.resolve("sub/dir/mvn.json");
        var config = new MvnJsonConfig();
        config.setRepositories(java.util.List.of(new MvnJsonConfig.Repository("id", "url")));

        MvnJsonParser.write(path, config);

        assertThat(path).exists();
        var reRead = MvnJsonParser.read(path);
        assertThat(reRead.getRepositories()).hasSize(1);
    }

    @Test
    void emptyRepositoriesListRoundtrip(@TempDir Path tempDir) throws IOException {
        var config = new MvnJsonConfig();
        config.setRepositories(java.util.List.of());

        var path = tempDir.resolve("mvn.json");
        MvnJsonParser.write(path, config);

        var reRead = MvnJsonParser.read(path);
        assertThat(reRead.getRepositories()).isEmpty();
    }
}
