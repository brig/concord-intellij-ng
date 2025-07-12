package brig.concord.structureView;

import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Original class is needed to fix Autoscroll from Source functionality.
 * Autoscroll works correctly if structure view element has {@link PsiElement} as a return type of
 * {@link StructureViewTreeElement#getValue()}
 */
class YAMLStructureViewKeyValue extends TreeElementBase<YAMLKeyValue> {

    private final Icon icon;
    private final boolean alwaysLeaf;

    public YAMLStructureViewKeyValue(@NotNull YAMLKeyValue kv) {
        this(kv, null, false);
    }

    public YAMLStructureViewKeyValue(@NotNull YAMLKeyValue kv, Icon icon) {
        this(kv, icon, false);
    }

    public YAMLStructureViewKeyValue(@NotNull YAMLKeyValue kv, Icon icon, boolean alwaysLeaf) {
        super(kv);
        this.icon = icon;
        this.alwaysLeaf = alwaysLeaf;
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        if (alwaysLeaf) {
            return List.of();
        }
        return ConcordStructureViewFactory.createChildrenViewTreeElements(getKeyValue().getValue());
    }

    @Override
    public String getLocationString() {
        var kv = getKeyValue();
        var value = kv.getValue();
        if (value instanceof YAMLScalar) {
            return kv.getValueText();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable String getPresentableText() {
        return getKeyValue().getKeyText();
    }

    @Override
    public Icon getIcon(boolean open) {
        if (icon != null) {
            return icon;
        }

        var kv = getKeyValue();
        var value = kv.getValue();
        if (value instanceof YAMLScalar) {
            return kv.getIcon(0);
        } else {
            return PlatformIcons.XML_TAG_ICON;
        }
    }

    public boolean isAlwaysLeaf() {
        return alwaysLeaf;
    }

    @Override
    public String toString() {
        return getKeyValue().getKeyText();
    }

    protected @NotNull YAMLKeyValue getKeyValue() {
        return Objects.requireNonNull(getElement());
    }
}
