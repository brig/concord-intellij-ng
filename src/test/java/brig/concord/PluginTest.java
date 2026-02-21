// SPDX-License-Identifier: Apache-2.0
package brig.concord;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PluginTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testPluginIsLoaded() {
        PluginId pluginId = PluginId.getId("brig.concord.intellij");
        Assertions.assertNotNull(PluginManagerCore.getPlugin(pluginId), "Plugin must be loaded");
    }
}
