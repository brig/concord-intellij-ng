package brig.concord.structureView;

import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLSequenceItem;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Objects;

/**
 * Original class is needed to fix Autoscroll from Source functionality.
 * Autoscroll works correctly if structure view element has {@link PsiElement} as a return type of
 * {@link StructureViewTreeElement#getValue()}
 */
class YAMLStructureViewSequenceItem extends TreeElementBase<YAMLSequenceItem> {

    private final Icon icon;

    YAMLStructureViewSequenceItem(@NotNull YAMLSequenceItem item) {
        this(item, null);
    }

    YAMLStructureViewSequenceItem(@NotNull YAMLSequenceItem item, Icon icon) {
        super(item);
        this.icon = icon;
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        return ConcordStructureViewFactory.createChildrenViewTreeElements(getItemValue());
    }

    @Override
    public @Nullable String getPresentableText() {
        var itemValue = getItemValue();
        if (itemValue instanceof YAMLScalar scalar) {
            return scalar.getTextValue();
        }
        return ConcordBundle.message("YAMLStructureViewSequenceItem.element.name");
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
        if (icon != null) {
            return icon;
        }

        var itemValue = getItemValue();
        if (itemValue instanceof YAMLScalar) {
            return PlatformIcons.PROPERTY_ICON;
        } else {
            return PlatformIcons.XML_TAG_ICON;
        }
    }

    private YAMLValue getItemValue() {
        return Objects.requireNonNull(getElement()).getValue();
    }
}
