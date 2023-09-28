package brig.concord;

import brig.concord.psi.ConcordFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlowUsageTypeProvider implements UsageTypeProvider {
    @Override
    public @Nullable UsageType getUsageType(@NotNull PsiElement element) {
        PsiFile psiFile = element.getContainingFile();
        if (psiFile instanceof ConcordFile) {
            return FLOW_CALL_USAGE_TYPE;
        }
        return null;
    }

    private static final UsageType FLOW_CALL_USAGE_TYPE = new UsageType(() -> ConcordBundle.message("flawCall.usage.type"));
}
