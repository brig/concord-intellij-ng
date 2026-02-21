// SPDX-License-Identifier: Apache-2.0
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

public class TriggersView extends YAMLStructureViewKeyValue {

    public TriggersView(@NotNull YAMLKeyValue kv) {
        super(kv, ConcordIcons.TRIGGERS);
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
                    .map(kvs -> new YAMLStructureViewKeyValue(kvs.iterator().next(), ConcordIcons.TRIGGER))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
