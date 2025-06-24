package brig.concord.inspection;

import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.psi.ConcordFile;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.impl.YamlMetaTypeProvider;
import brig.concord.yaml.meta.impl.YamlMissingKeysInspectionBase;

public class MissingKeysInspection extends YamlMissingKeysInspectionBase {

    @Override
    protected @Nullable YamlMetaTypeProvider getMetaTypeProvider(@NotNull ProblemsHolder holder) {
        return ConcordMetaTypeProvider.getInstance(holder.getProject());
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        if (holder.getFile() instanceof ConcordFile) {
            return super.buildVisitor(holder, isOnTheFly, session);
        } else {
            return new PsiElementVisitor() {
            };
        }
    }
}
