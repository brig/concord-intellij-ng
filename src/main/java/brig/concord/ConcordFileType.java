// SPDX-License-Identifier: Apache-2.0
package brig.concord;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConcordFileType extends LanguageFileType {

    public static final ConcordFileType INSTANCE = new ConcordFileType();

    private ConcordFileType() {
        super(ConcordLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Concord File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return ConcordBundle.message("filetype.description.yaml");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "concord.yaml";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ConcordIcons.FILE;
    }
}
