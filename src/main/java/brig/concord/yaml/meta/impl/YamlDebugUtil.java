package brig.concord.yaml.meta.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class YamlDebugUtil {
    static @NotNull String getDebugInfo(@Nullable PsiElement psi) {
        if (psi == null) {
            return "<null>";
        }

        String text = psi.getText();
        if (text.contains("\n")) {
            int firstEol = text.indexOf('\n');
            int lastEol = text.lastIndexOf('\n');
            if (firstEol >= 0) {
                text = text.substring(0, firstEol) + " ... " + text.substring(lastEol + 1);
            }
        }

        return psi + ", range: " + psi.getTextRange() + ", text: `" + text + "`";
    }
}

