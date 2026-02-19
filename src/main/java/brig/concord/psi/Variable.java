package brig.concord.psi;

import brig.concord.schema.SchemaProperty;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Variable(@NotNull String name, @NotNull VariableSource source,
                       @Nullable PsiElement declaration, @NotNull SchemaProperty schema) {}
