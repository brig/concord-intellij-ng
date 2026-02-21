// SPDX-License-Identifier: Apache-2.0
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class ImportsDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteImports() {
        configureFromText("""
                flows:
                  main:
                    - log: "Hello"

                imports:
                  - git:
                      url: "https://github.com/walmartlabs/concord.git"
                      path: "examples/hello_world"
                      version: "master"
                      name: "test"
                      dest: "/concord"
                      exclude:
                        - "/one"
                      secret:
                        name: "my-secret"
                        org: "secret-org"
                        password: "test"
                  - mvn:
                      url: "mvn://groupId:artifactId:version"
                      dest: "/"
                  - dir:
                      src: "source"
                      dest: "destination"
                """);

        assertDocTarget(key("/imports"), "doc.imports.description",
                "/documentation/imports.html");

        assertDocTarget(key("/imports[0]/git"), "doc.imports.git.description",
                "/documentation/imports.git.html");
        assertDocTarget(key("/imports[0]/git/url"), "doc.imports.git.url.description",
                "/documentation/imports.git.url.html");
        assertDocTarget(key("/imports[0]/git/path"), "doc.imports.git.path.description",
                "/documentation/imports.git.path.html");
        assertDocTarget(key("/imports[0]/git/version"), "doc.imports.git.version.description",
                "/documentation/imports.git.version.html");
        assertDocTarget(key("/imports[0]/git/name"), "doc.imports.git.name.description",
                "/documentation/imports.git.name.html");
        assertDocTarget(key("/imports[0]/git/dest"), "doc.imports.git.dest.description",
                "/documentation/imports.git.dest.html");
        assertDocTarget(key("/imports[0]/git/exclude"), "doc.imports.git.exclude.description",
                "/documentation/imports.git.exclude.html");
        assertNoDocTarget(value("/imports[0]/git/exclude[0]"));
        assertDocTarget(key("/imports[0]/git/secret"), "doc.imports.git.secret.description",
                "/documentation/imports.git.secret.html");
        assertDocTarget(key("/imports[0]/git/secret/name"), "doc.imports.git.secret.name.description",
                "/documentation/imports.git.secret.name.html");
        assertDocTarget(key("/imports[0]/git/secret/org"), "doc.imports.git.secret.org.description",
                "/documentation/imports.git.secret.org.html");
        assertDocTarget(key("/imports[0]/git/secret/password"), "doc.imports.git.secret.password.description",
                "/documentation/imports.git.secret.password.html");

        assertDocTarget(key("/imports[1]/mvn"), "doc.imports.mvn.description",
                "/documentation/imports.mvn.html");
        assertDocTarget(key("/imports[1]/mvn/url"), "doc.imports.mvn.url.description",
                "/documentation/imports.mvn.url.html");
        assertDocTarget(key("/imports[1]/mvn/dest"), "doc.imports.mvn.dest.description",
                "/documentation/imports.mvn.dest.html");

        assertDocTarget(key("/imports[2]/dir"), "doc.imports.dir.description",
                "/documentation/imports.dir.html");
        assertDocTarget(key("/imports[2]/dir/src"), "doc.imports.dir.src.description",
                "/documentation/imports.dir.src.html");
        assertDocTarget(key("/imports[2]/dir/dest"), "doc.imports.dir.dest.description",
                "/documentation/imports.dir.dest.html");
    }
}
