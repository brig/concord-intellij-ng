// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.run;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConcordRunConfigurationOptions extends RunConfigurationOptions {

    private final StoredProperty<String> myEntryPoint = string("").provideDelegate(this, "entryPoint");
    private final StoredProperty<String> myWorkingDirectory = string("").provideDelegate(this, "workingDirectory");
    private final StoredProperty<String> myAdditionalArguments = string("").provideDelegate(this, "additionalArguments");
    private final StoredProperty<Map<Object, Object>> myParameters = map().provideDelegate(this, "parameters");

    public @NotNull String getEntryPoint() {
        var value = myEntryPoint.getValue(this);
        return value != null ? value : "";
    }

    public void setEntryPoint(@Nullable String entryPoint) {
        myEntryPoint.setValue(this, entryPoint != null ? entryPoint : "");
    }

    public @NotNull String getWorkingDirectory() {
        var value = myWorkingDirectory.getValue(this);
        return value != null ? value : "";
    }

    public void setWorkingDirectory(@Nullable String workingDirectory) {
        myWorkingDirectory.setValue(this, workingDirectory != null ? workingDirectory : "");
    }

    public @NotNull String getAdditionalArguments() {
        var value = myAdditionalArguments.getValue(this);
        return value != null ? value : "";
    }

    public void setAdditionalArguments(@Nullable String additionalArguments) {
        myAdditionalArguments.setValue(this, additionalArguments != null ? additionalArguments : "");
    }

    public @NotNull Map<String, String> getParameters() {
        var value = myParameters.getValue(this);
        var result = new LinkedHashMap<String, String>();
        for (var entry : value.entrySet()) {
            if (entry.getKey() instanceof String k && entry.getValue() instanceof String v) {
                result.put(k, v);
            }
        }
        return result;
    }

    public void setParameters(@Nullable Map<String, String> parameters) {
        var map = new LinkedHashMap<>();
        if (parameters != null) {
            map.putAll(parameters);
        }
        myParameters.setValue(this, map);
    }
}
