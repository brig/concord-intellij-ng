// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.el.psi;

import brig.concord.el.ElFileType;
import brig.concord.el.ElLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class ElFile extends PsiFileBase {

    public ElFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, ElLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return ElFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "EL Expression File";
    }
}
