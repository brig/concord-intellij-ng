// SPDX-License-Identifier: Apache-2.0
package brig.concord.structureView;

import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ResourcesView extends YAMLStructureViewKeyValue {

    public ResourcesView(@NotNull YAMLKeyValue kv) {
        super(kv, ConcordIcons.RESOURCES);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        if (getKeyValue().getValue() instanceof YAMLMapping mapping) {
            return ContainerUtil.map(mapping.getKeyValues(), YAMLStructureViewKeyValue::new);
        }
        return List.of();
    }
}
