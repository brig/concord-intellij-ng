package brig.concord.structureView;

import brig.concord.psi.ConcordFile;
import brig.concord.yaml.psi.*;
import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class ConcordStructureViewFactory implements PsiStructureViewFactory {

    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        if (!(psiFile instanceof YAMLFile)) {
            return null;
        }

        return new TreeBasedStructureViewBuilder() {
            @Override
            public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new ConcordStructureViewModel(psiFile, editor, new ConcordStructureViewFile((ConcordFile) psiFile))
                        .withSorters(Sorter.ALPHA_SORTER)
                        .withSuitableClasses(ConcordFile.class, YAMLKeyValue.class);
            }
        };
    }

    private static class ConcordStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {

        public ConcordStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor, @NotNull StructureViewTreeElement root) {
            super(psiFile, editor, root);
        }

        @Override
        public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
            return false;
        }

        @Override
        public boolean isAlwaysLeaf(StructureViewTreeElement element) {
            if (element instanceof YAMLStructureViewKeyValue kv) {
                return kv.isAlwaysLeaf();
            }
            return false;
        }
    }

    static @NotNull Collection<StructureViewTreeElement> createChildrenViewTreeElements(@Nullable YAMLPsiElement element) {
        if (element == null) {
            return Collections.emptyList();
        }

        Ref<Collection<StructureViewTreeElement>> result = Ref.create(Collections.emptyList());
        element.accept(new YamlPsiElementVisitor() {
            @Override
            public void visitSequence(@NotNull YAMLSequence sequence) {
                result.set(ContainerUtil.map(sequence.getItems(), YAMLStructureViewSequenceItem::new));
            }

            @Override
            public void visitMapping(@NotNull YAMLMapping mapping) {
                result.set(ContainerUtil.map(mapping.getKeyValues(), YAMLStructureViewKeyValue::new));
            }
        });

        return result.get();
    }
}
