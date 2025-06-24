package brig.concord.yaml.meta.model;

/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLScalar;

public abstract class YamlReferenceType extends YamlScalarType {

    /**
     * @deprecated initialise the {@code displayName} explicitly via {@link #YamlReferenceType(String, String)}
     */
    @Deprecated(forRemoval = true)
    protected YamlReferenceType(@NotNull String typeName) {
        super(typeName);
    }

    protected YamlReferenceType(@NotNull String typeName, @NotNull String displayName) {
        super(typeName, displayName);
    }

    public PsiReference @NotNull [] getReferencesFromValue(@NotNull YAMLScalar valueScalar) {
        return PsiReference.EMPTY_ARRAY;
    }
}
