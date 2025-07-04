package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLAnchor;

public class YAMLElementDescriptionProvider implements ElementDescriptionProvider {
    @Override
    public @Nullable String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
        if (location instanceof UsageViewTypeLocation) {
            if (element instanceof YAMLAnchor) {
                return ConcordBundle.message("YAMLElementDescriptionProvider.type.anchor");
            }
        }
        return null;
    }
}
