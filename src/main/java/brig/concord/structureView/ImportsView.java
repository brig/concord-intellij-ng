package brig.concord.structureView;

import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLSequence;
import brig.concord.yaml.psi.YAMLSequenceItem;
import com.intellij.ide.structureView.StructureViewTreeElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ImportsView extends YAMLStructureViewKeyValue {

    public ImportsView(@NotNull YAMLKeyValue kv) {
        super(kv, ConcordIcons.IMPORTS);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        if (getKeyValue().getValue() instanceof YAMLSequence seq) {
            return seq.getItems().stream()
                    .map(YAMLSequenceItem::getValue)
                    .filter(YAMLMapping.class::isInstance)
                    .map(YAMLMapping.class::cast)
                    .map(YAMLMapping::getKeyValues)
                    .filter(kvs -> !kvs.isEmpty())
                    .map(kvs -> new YAMLStructureViewKeyValue(kvs.iterator().next(), ConcordIcons.IMPORT))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
