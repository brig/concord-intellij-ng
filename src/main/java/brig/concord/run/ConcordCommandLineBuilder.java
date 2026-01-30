package brig.concord.run;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for building Concord CLI command line parameters.
 * Extracted for testability.
 */
public final class ConcordCommandLineBuilder {

    private ConcordCommandLineBuilder() {
    }

    /**
     * Result of building command line parameters.
     */
    public record BuildResult(
            @Nullable String entryPoint,
            @NotNull Map<String, String> parameters,
            @NotNull List<String> profiles
    ) {
    }

    /**
     * Builds command line parameters based on run mode and configuration.
     *
     * @param configuredEntryPoint entry point from run configuration (flow name)
     * @param configurationParams  parameters from run configuration
     * @param isDelegatingMode     whether delegating mode is enabled
     * @param mainEntryPoint       main entry point from settings (used in delegating mode)
     * @param flowParameterName    parameter name for flow (used in delegating mode)
     * @param defaultParameters    default parameters from settings
     * @param activeProfiles       active profiles from settings
     * @return build result with entry point, merged parameters, and profiles
     */
    public static @NotNull BuildResult buildParameters(
            @NotNull String configuredEntryPoint,
            @NotNull Map<String, String> configurationParams,
            boolean isDelegatingMode,
            @NotNull String mainEntryPoint,
            @NotNull String flowParameterName,
            @NotNull Map<String, String> defaultParameters,
            @NotNull List<String> activeProfiles
    ) {
        // Determine entry point based on run mode
        String entryPoint;
        if (isDelegatingMode) {
            entryPoint = mainEntryPoint.isBlank() ? null : mainEntryPoint;
        } else {
            entryPoint = configuredEntryPoint.isBlank() ? null : configuredEntryPoint;
        }

        // Merge parameters: defaults first, then flow param (delegating), then user params
        var mergedParams = new LinkedHashMap<String, String>();
        mergedParams.putAll(defaultParameters);

        // In delegating mode: add flow name as parameter
        if (isDelegatingMode && !configuredEntryPoint.isBlank() && !flowParameterName.isBlank()) {
            mergedParams.put(flowParameterName, configuredEntryPoint);
        }

        // User params override everything
        mergedParams.putAll(configurationParams);

        return new BuildResult(entryPoint, mergedParams, activeProfiles);
    }
}
