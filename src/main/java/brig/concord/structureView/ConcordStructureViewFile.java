package brig.concord.structureView;

import brig.concord.psi.ConcordFile;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.treeView.smartTree.SorterUtil;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class ConcordStructureViewFile extends PsiTreeElementBase<ConcordFile> {

    private final List<ViewElementProducer> viewElementProducers = List.of(
            (ConcordFile concordFile) -> concordFile.flows().map(FlowsView::new),
            (ConcordFile concordFile) -> concordFile.configuration().map(ConfigurationView::new),
            (ConcordFile concordFile) -> concordFile.forms().map(FormsView::new),
            (ConcordFile concordFile) -> concordFile.resources().map(ResourcesView::new),
            (ConcordFile concordFile) -> concordFile.imports().map(ImportsView::new),
            (ConcordFile concordFile) -> concordFile.triggersKv().map(TriggersView::new),
            (ConcordFile concordFile) -> concordFile.publicFlows().map(PublicFlowsView::new),
            (ConcordFile concordFile) -> concordFile.profiles().map(ProfilesView::new)
    );

    ConcordStructureViewFile(ConcordFile psiElement) {
        super(psiElement);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        var file = Objects.requireNonNull(getElement());

        return viewElementProducers.stream()
                .map(p -> p.produce(file))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(SorterUtil::getStringPresentation, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPresentableText() {
        return getFilePresentation().getPresentableText();
    }

    @Override
    public @Nullable String getLocationString() {
        return getFilePresentation().getLocationString();
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
        return getFilePresentation().getIcon(unused);
    }

    private @NotNull ItemPresentation getFilePresentation() {
        var file = Objects.requireNonNull(getElement());
        return Objects.requireNonNull(file.getPresentation());
    }

    public interface ViewElementProducer {

        Optional<? extends StructureViewTreeElement> produce(ConcordFile concordFile);
    }
}
