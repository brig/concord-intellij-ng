package brig.concord.structureView;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.NodeDescriptorProvidingKey;
import com.intellij.ide.util.treeView.TreeAnchorizer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.swing.*;
import java.util.*;

public abstract class TreeElementBase<T extends PsiElement> implements StructureViewTreeElement, ItemPresentation, NodeDescriptorProvidingKey {

    private final Object myValue;

    protected TreeElementBase(T psiElement) {
        myValue = psiElement == null ? null : TreeAnchorizer.getService().createAnchor(psiElement);
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return this;
    }

    @Override
    public @NotNull Object getKey() {
        return String.valueOf(getElement());
    }

    public final @Nullable T getElement() {
        //noinspection unchecked
        return myValue == null ? null : (T) TreeAnchorizer.getService().retrieveElement(myValue);
    }

    @Override
    public Icon getIcon(boolean open) {
        final PsiElement element = getElement();
        if (element != null) {
            int flags = Iconable.ICON_FLAG_READ_STATUS;
            if (!(element instanceof PsiFile) || !element.isWritable()) flags |= Iconable.ICON_FLAG_VISIBILITY;
            return element.getIcon(flags);
        } else {
            return null;
        }
    }

    @Override
    public T getValue() {
        return getElement();
    }

    @Override
    public String toString() {
        final T element = getElement();
        return element != null ? element.toString() : "";
    }

    @Override
    public final StructureViewTreeElement @NotNull [] getChildren() {
        List<StructureViewTreeElement> list = doGetChildren();
        return list.isEmpty() ? EMPTY_ARRAY : list.toArray(EMPTY_ARRAY);
    }

    private @NotNull List<StructureViewTreeElement> doGetChildren() {
        T element = getElement();
        if (element == null) {
            return Collections.emptyList();
        }

        Collection<StructureViewTreeElement> baseChildren = getChildrenBase();
        return (baseChildren instanceof List<StructureViewTreeElement> list) ? list : new ArrayList<>(baseChildren);
    }

    @Override
    public void navigate(boolean requestFocus) {
        T element = getElement();
        if (element != null) {
            ((Navigatable) element).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        final T element = getElement();
        return element instanceof Navigatable && ((Navigatable) element).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    public abstract @Unmodifiable @NotNull Collection<StructureViewTreeElement> getChildrenBase();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeElementBase<?> that = (TreeElementBase<?>) o;
        return Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        T value = getValue();
        return value == null ? 0 : value.hashCode();
    }
}
