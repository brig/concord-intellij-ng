// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ConcordBuiltInVars {

    public record BuiltInVar(@NotNull String name, @NotNull String description) {}

    public static final List<BuiltInVar> VARS = List.of(
            new BuiltInVar("txId", "Unique identifier of the current process"),
            new BuiltInVar("parentInstanceId", "Identifier of the parent process"),
            new BuiltInVar("workDir", "Path to the working directory"),
            new BuiltInVar("initiator", "Information about the user who started the process"),
            new BuiltInVar("currentUser", "Information about the current user"),
            new BuiltInVar("requestInfo", "Additional request data"),
            new BuiltInVar("projectInfo", "Project data"),
            new BuiltInVar("processInfo", "Current process information")
    );

    private ConcordBuiltInVars() {}
}
