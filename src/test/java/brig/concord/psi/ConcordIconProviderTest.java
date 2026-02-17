// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi;

import brig.concord.ConcordIcons;
import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;

class ConcordIconProviderTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testSectionIcons() {
        configureFromText("""
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Hello"
                forms:
                  myForm: []
                triggers:
                  - github: {}
                """);

        var iconProvider = new ConcordIconProvider();

        // Check sections
        assertIcon(iconProvider, key("/flows"), ConcordIcons.FLOWS);
        assertIcon(iconProvider, key("/forms"), ConcordIcons.FORMS);
        assertIcon(iconProvider, key("/triggers"), ConcordIcons.TRIGGERS);
        assertIcon(iconProvider, key("/configuration"), ConcordIcons.CONFIGURATION);
    }

    @Test
    void testElementIcons() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "Hello"
                forms:
                  myForm:
                    - name: field1
                """);

        var iconProvider = new ConcordIconProvider();

        // Check elements inside sections
        assertIcon(iconProvider, key("/flows/myFlow"), ConcordIcons.FLOW);
        assertIcon(iconProvider, key("/forms/myForm"), ConcordIcons.FORM);
    }

    private void assertIcon(ConcordIconProvider provider, KeyTarget target, Icon expectedIcon) {
        ReadAction.run(() -> {
            var file = myFixture.getFile();
            Assertions.assertInstanceOf(ConcordFile.class, file, "File should be a ConcordFile");

            var targetKv = target.asKeyValue();

            var icon = provider.getIcon(targetKv, 0);
            Assertions.assertEquals(expectedIcon, icon, "Wrong icon for key: " + targetKv.getKeyText());
        });
    }
}
