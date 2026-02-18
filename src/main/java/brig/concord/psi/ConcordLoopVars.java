package brig.concord.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ConcordLoopVars {

    public record LoopVar(@NotNull String name, @NotNull String description) {}

    public static final List<LoopVar> VARS = List.of(
            new LoopVar("item", "Current item of the loop iteration"),
            new LoopVar("items", "Collection of items being iterated"),
            new LoopVar("itemIndex", "Zero-based index of the current iteration")
    );

    private ConcordLoopVars() {}
}
