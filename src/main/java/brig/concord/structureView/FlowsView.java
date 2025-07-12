package brig.concord.structureView;

import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class FlowsView extends YAMLStructureViewKeyValue {

    public FlowsView(@NotNull YAMLKeyValue kv) {
        super(kv, ConcordIcons.FLOWS);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        if (getKeyValue().getValue() instanceof YAMLMapping mapping) {
            return ContainerUtil.map(mapping.getKeyValues(), kv -> new YAMLStructureViewKeyValue(kv, ConcordIcons.FLOW, true));
        }
        return List.of();
    }
}
