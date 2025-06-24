package brig.concord;

import com.intellij.codeInspection.InspectionEP;
import com.intellij.codeInspection.LocalInspectionEP;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class PluginTest extends BasePlatformTestCase {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testPluginIsLoaded() {
        PluginId pluginId = PluginId.getId("brig.concord.intellij");
        assertNotNull("Plugin must be loaded", PluginManagerCore.getPlugin(pluginId));

        System.out.println("Loaded extensions:");
        InspectionEP[] eps = Extensions.getExtensions(LocalInspectionEP.LOCAL_INSPECTION);
        for (InspectionEP ep : eps) {
            System.out.println(ep.implementationClass);
        }
    }
}
