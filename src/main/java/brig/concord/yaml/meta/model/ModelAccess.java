package brig.concord.yaml.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLDocument;

public interface ModelAccess {
    @Nullable
    Field getRoot(@NotNull YAMLDocument document);
}
