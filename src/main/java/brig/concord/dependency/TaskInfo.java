// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TaskInfo(
        @NotNull String name,
        @NotNull List<TaskMethod> methods) {
}
