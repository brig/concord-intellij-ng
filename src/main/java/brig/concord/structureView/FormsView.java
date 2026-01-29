package brig.concord.structureView;

import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLSequence;
import brig.concord.yaml.psi.YAMLSequenceItem;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FormsView extends YAMLStructureViewKeyValue {

    public FormsView(@NotNull YAMLKeyValue kv) {
        super(kv, ConcordIcons.FORMS);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        if (getKeyValue().getValue() instanceof YAMLMapping mapping) {
            return ContainerUtil.map(mapping.getKeyValues(), FormView::new);
        }
        return List.of();
    }

    static class FormView extends YAMLStructureViewKeyValue {

        protected FormView(YAMLKeyValue psiElement) {
            super(psiElement);
        }

        @Override
        public @Unmodifiable @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
            if (getKeyValue().getValue() instanceof YAMLSequence seq) {
                return seq.getItems().stream()
                        .map(YAMLSequenceItem::getValue)
                        .filter(YAMLMapping.class::isInstance)
                        .map(YAMLMapping.class::cast)
                        .map(YAMLMapping::getKeyValues)
                        .filter(kvs -> !kvs.isEmpty())
                        .map(kvs -> new YAMLStructureViewKeyValue(kvs.iterator().next(), ConcordIcons.FORM_FIELD))
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        @Override
        public Icon getIcon(boolean open) {
            return ConcordIcons.FORM;
        }
    }
}
