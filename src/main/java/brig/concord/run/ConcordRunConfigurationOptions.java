package brig.concord.run;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConcordRunConfigurationOptions extends RunConfigurationOptions {

    private final StoredProperty<String> myEntryPoint = string("").provideDelegate(this, "entryPoint");
    private final StoredProperty<String> myWorkingDirectory = string("").provideDelegate(this, "workingDirectory");
    private final StoredProperty<String> myAdditionalArguments = string("").provideDelegate(this, "additionalArguments");
    private final StoredProperty<String> myParametersEncoded = string("").provideDelegate(this, "parameters");

    private static final String PARAM_SEPARATOR = "\u001F";
    private static final String KV_SEPARATOR = "\u001E";

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
        var encoded = myParametersEncoded.getValue(this);
        var result = new LinkedHashMap<String, String>();
        if (encoded != null && !encoded.isEmpty()) {
            for (var entry : encoded.split(PARAM_SEPARATOR)) {
                var idx = entry.indexOf(KV_SEPARATOR);
                if (idx > 0) {
                    result.put(entry.substring(0, idx), entry.substring(idx + 1));
                } else if (!entry.isEmpty()) {
                    result.put(entry, "");
                }
            }
        }
        return result;
    }

    public void setParameters(@Nullable Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            myParametersEncoded.setValue(this, "");
            return;
        }

        var entries = new ArrayList<String>();
        for (var entry : parameters.entrySet()) {
            entries.add(entry.getKey() + KV_SEPARATOR + entry.getValue());
        }
        myParametersEncoded.setValue(this, String.join(PARAM_SEPARATOR, entries));
    }
}
