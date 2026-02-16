package brig.concord.el;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ElFileType extends LanguageFileType {

    public static final ElFileType INSTANCE = new ElFileType();

    private ElFileType() {
        super(ElLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "concord-el";
    }

    @Override
    public @NotNull String getDescription() {
        return "Expression language";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }
}
