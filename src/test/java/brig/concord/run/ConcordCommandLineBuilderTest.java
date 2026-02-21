// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.run;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ConcordCommandLineBuilderTest {

    @Test
    void testDirectMode_usesConfigEntryPoint() {
        var result = ConcordCommandLineBuilder.buildParameters(
                "myFlow",                           // configuredEntryPoint
                Map.of("key1", "value1"),           // configurationParams
                false,                               // isDelegatingMode
                "default",                           // mainEntryPoint
                "flow",                              // flowParameterName
                Map.of("debug", "true"),             // defaultParameters
                List.of("dev")                       // activeProfiles
        );

        Assertions.assertEquals("myFlow", result.entryPoint());
        Assertions.assertEquals(Map.of("debug", "true", "key1", "value1"), result.parameters());
        Assertions.assertEquals(List.of("dev"), result.profiles());
    }

    @Test
    void testDelegatingMode_usesMainEntryPointAndFlowParam() {
        var result = ConcordCommandLineBuilder.buildParameters(
                "myFlow",                           // configuredEntryPoint
                Map.of("key1", "value1"),           // configurationParams
                true,                                // isDelegatingMode
                "default",                           // mainEntryPoint
                "flow",                              // flowParameterName
                Map.of("debug", "true"),             // defaultParameters
                List.of("dev")                       // activeProfiles
        );

        Assertions.assertEquals("default", result.entryPoint());
        // flow param should be added, then user params override
        var expectedParams = new LinkedHashMap<String, String>();
        expectedParams.put("debug", "true");
        expectedParams.put("flow", "myFlow");
        expectedParams.put("key1", "value1");
        Assertions.assertEquals(expectedParams, result.parameters());
    }

    @Test
    void testDelegatingMode_userParamsOverrideFlowParam() {
        var result = ConcordCommandLineBuilder.buildParameters(
                "myFlow",                           // configuredEntryPoint
                Map.of("flow", "overridden"),       // configurationParams - overrides flow
                true,                                // isDelegatingMode
                "default",                           // mainEntryPoint
                "flow",                              // flowParameterName
                Map.of(),                            // defaultParameters
                List.of()                            // activeProfiles
        );

        Assertions.assertEquals("default", result.entryPoint());
        // user's explicit flow param should override
        Assertions.assertEquals(Map.of("flow", "overridden"), result.parameters());
    }

    @Test
    void testDelegatingMode_userParamsOverrideDefaultParams() {
        var result = ConcordCommandLineBuilder.buildParameters(
                "myFlow",                           // configuredEntryPoint
                Map.of("debug", "false"),           // configurationParams - overrides default
                true,                                // isDelegatingMode
                "default",                           // mainEntryPoint
                "flow",                              // flowParameterName
                Map.of("debug", "true"),             // defaultParameters
                List.of()                            // activeProfiles
        );

        // user params should override both default params and flow param
        var expectedParams = new LinkedHashMap<String, String>();
        expectedParams.put("debug", "false");
        expectedParams.put("flow", "myFlow");
        Assertions.assertEquals(expectedParams, result.parameters());
    }

    @Test
    void testDirectMode_emptyEntryPoint() {
        var result = ConcordCommandLineBuilder.buildParameters(
                "",                                  // configuredEntryPoint - empty
                Map.of(),                            // configurationParams
                false,                               // isDelegatingMode
                "default",                           // mainEntryPoint
                "flow",                              // flowParameterName
                Map.of(),                            // defaultParameters
                List.of()                            // activeProfiles
        );

        Assertions.assertNull(result.entryPoint());
        Assertions.assertTrue(result.parameters().isEmpty());
    }

    @Test
    void testDelegatingMode_emptyMainEntryPoint() {
        var result = ConcordCommandLineBuilder.buildParameters(
                "myFlow",                           // configuredEntryPoint
                Map.of(),                            // configurationParams
                true,                                // isDelegatingMode
                "",                                  // mainEntryPoint - empty
                "flow",                              // flowParameterName
                Map.of(),                            // defaultParameters
                List.of()                            // activeProfiles
        );

        Assertions.assertNull(result.entryPoint());
        Assertions.assertEquals(Map.of("flow", "myFlow"), result.parameters());
    }
}
