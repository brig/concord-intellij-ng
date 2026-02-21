// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class ProfilesDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteProfiles() {
        configureFromText("""
                flows:
                  main:
                    - log: "Hello"

                profiles:
                  staging:
                    configuration:
                      entryPoint: "stagingFlow"
                      arguments:
                        env: "staging"
                    flows:
                      stagingFlow:
                        - log: "staging"
                    forms:
                      myForm:
                        - name: { type: "string", label: "Name" }""");

        assertDocTarget(key("/profiles"), "doc.profiles.description",
                "/documentation/profiles.html");

        assertDocTarget(key("/profiles/staging"), "doc.profiles.profileName.description",
                "/documentation/profiles.profileName.html");

        assertDocTarget(key("/profiles/staging/configuration"), "doc.profile.configuration.description",
                "/documentation/profiles.profileName.configuration.html");

        assertDocTarget(key("/profiles/staging/flows"), "doc.flows.description",
                "/documentation/profiles.profileName.flows.html");

        assertDocTarget(key("/profiles/staging/forms"), "doc.forms.description",
                "/documentation/profiles.profileName.forms.html");
    }
}
