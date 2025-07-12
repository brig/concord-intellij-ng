package brig.concord.structureView;

import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ProfilesView extends YAMLStructureViewKeyValue {

    public ProfilesView(@NotNull YAMLKeyValue kv) {
        super(kv, ConcordIcons.PROFILES);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        if (getKeyValue().getValue() instanceof YAMLMapping mapping) {
            return ContainerUtil.map(mapping.getKeyValues(), kv -> new YAMLStructureViewKeyValue(kv, ConcordIcons.PROFILE, true));
        }
        return List.of();
    }
}
