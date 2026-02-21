// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import org.jetbrains.annotations.NotNull;

public enum VariableSource {
    BUILT_IN("built-in", "built-in variable"),
    ARGUMENT("argument", "process argument"),
    FLOW_PARAMETER("flow in", "flow input parameter"),
    SET_STEP("set", "set step variable"),
    STEP_OUT("step out", "step output variable"),
    LOOP("loop", "loop variable"),
    TASK_RESULT("task result", "task result variable");

    private final String shortLabel;
    private final String description;

    VariableSource(String shortLabel, String description) {
        this.shortLabel = shortLabel;
        this.description = description;
    }

    public @NotNull String shortLabel() {
        return shortLabel;
    }

    public @NotNull String description() {
        return description;
    }
}
