// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import brig.concord.ConcordYamlTestBaseJunit5;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConcordRunModeSettingsTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void defaultValues() {
        var settings = ConcordRunModeSettings.getInstance(getProject());

        assertEquals(ConcordRunModeSettings.RunMode.DIRECT, settings.getRunMode());
        assertEquals("default", settings.getMainEntryPoint());
        assertEquals("flow", settings.getFlowParameterName());
        assertEquals(ConcordRunModeSettings.DEFAULT_TARGET_DIR, settings.getTargetDir());
        assertEquals(List.of(), settings.getActiveProfiles());
        assertEquals(Map.of(), settings.getDefaultParameters());
        assertFalse(settings.isDelegatingMode());
    }

    @Test
    void targetDir_roundTrip() {
        var settings = ConcordRunModeSettings.getInstance(getProject());
        settings.setTargetDir("custom/target");

        assertEquals("custom/target", settings.getTargetDir());

        // Simulate persistence round-trip
        var state = settings.getState();
        var fresh = new ConcordRunModeSettings();
        fresh.loadState(state);

        assertEquals("custom/target", fresh.getTargetDir());
    }

    @Test
    void delegatingMode() {
        var settings = ConcordRunModeSettings.getInstance(getProject());

        assertFalse(settings.isDelegatingMode());

        settings.setRunMode(ConcordRunModeSettings.RunMode.DELEGATING);
        assertTrue(settings.isDelegatingMode());

        settings.setRunMode(ConcordRunModeSettings.RunMode.DIRECT);
        assertFalse(settings.isDelegatingMode());
    }

    @Test
    void allFields_roundTrip() {
        var settings = ConcordRunModeSettings.getInstance(getProject());
        settings.setRunMode(ConcordRunModeSettings.RunMode.DELEGATING);
        settings.setMainEntryPoint("main");
        settings.setFlowParameterName("flowName");
        settings.setTargetDir("out/dir");
        settings.setActiveProfiles(List.of("dev", "test"));
        settings.setDefaultParameters(Map.of("debug", "true", "env", "staging"));

        var state = settings.getState();
        var fresh = new ConcordRunModeSettings();
        fresh.loadState(state);

        assertEquals(ConcordRunModeSettings.RunMode.DELEGATING, fresh.getRunMode());
        assertEquals("main", fresh.getMainEntryPoint());
        assertEquals("flowName", fresh.getFlowParameterName());
        assertEquals("out/dir", fresh.getTargetDir());
        assertEquals(List.of("dev", "test"), fresh.getActiveProfiles());
        assertEquals(Map.of("debug", "true", "env", "staging"), fresh.getDefaultParameters());
    }
}