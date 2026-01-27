package brig.concord.yaml.meta.impl;


import brig.concord.inspection.ConcordInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLSequenceItem;

public abstract class YamlMetaTypeInspectionBase extends ConcordInspectionTool {

    protected abstract @Nullable YamlMetaTypeProvider getMetaTypeProvider(@NotNull ProblemsHolder holder);

    protected abstract @NotNull PsiElementVisitor doBuildVisitor(@NotNull ProblemsHolder holder, @NotNull YamlMetaTypeProvider metaTypeProvider);

    @Override
    public final @NotNull PsiElementVisitor buildConcordVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        YamlMetaTypeProvider provider = getMetaTypeProvider(holder);
        return provider == null ? PsiElementVisitor.EMPTY_VISITOR
                : doBuildVisitor(holder, provider);
    }

    protected abstract static class SimpleYamlPsiVisitor extends PsiElementVisitor {
        @Override
        public void visitElement(@NotNull PsiElement element) {
            ProgressIndicatorProvider.checkCanceled();

            if (element instanceof YAMLKeyValue) {
                visitYAMLKeyValue((YAMLKeyValue)element);
            }
            else if (element instanceof YAMLMapping) {
                visitYAMLMapping((YAMLMapping)element);
            }
            else if (element instanceof YAMLSequenceItem) {
                visitYAMLSequenceItem((YAMLSequenceItem)element);
            }
            else if (element instanceof YAMLDocument) {
                visitYAMLDocument((YAMLDocument)element);
            }
        }

        protected void visitYAMLKeyValue(@NotNull YAMLKeyValue keyValue) {
        }

        protected void visitYAMLMapping(@NotNull YAMLMapping mapping) {
        }

        protected void visitYAMLDocument(@NotNull YAMLDocument document) {
        }

        protected void visitYAMLSequenceItem(@NotNull YAMLSequenceItem item) {
        }
    }
}
