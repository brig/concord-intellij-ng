// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryEditDialogTest {

    @Test
    void acceptsSupportedRepositoryUrlSchemes() {
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("https://repo.maven.apache.org/maven2")).isTrue();
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("http://repo.example.com/maven2")).isTrue();
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("file:///tmp/local-maven-repo")).isTrue();
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("file://localhost/tmp/local-maven-repo")).isTrue();
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("FILE:///tmp/local-maven-repo")).isTrue();
    }

    @Test
    void rejectsUnsupportedRepositoryUrlSchemes() {
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("ftp://repo.example.com/maven2")).isFalse();
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("/tmp/local-maven-repo")).isFalse();
        assertThat(RepositoryEditDialog.isSupportedRepositoryUrl("not a url")).isFalse();
    }
}
