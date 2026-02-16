package brig.concord.el.psi;

import brig.concord.el.ElLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ElTokenType extends IElementType {

    public ElTokenType(@NotNull @NonNls String debugName) {
        super(debugName, ElLanguage.INSTANCE);
    }
}
