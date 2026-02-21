// SPDX-License-Identifier: Apache-2.0
package brig.concord.structureView;

import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.ide.structureView.StructureViewTreeElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PublicFlowsView extends YAMLStructureViewKeyValue {

    public PublicFlowsView(@NotNull YAMLKeyValue kv) {
        super(kv, ConcordIcons.PUBLIC_FLOWS);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        if (getKeyValue().getValue() instanceof YAMLSequence seq) {
            return seq.getItems().stream()
                    .map(s -> new YAMLStructureViewSequenceItem(s, ConcordIcons.PUBLIC_FLOW))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
