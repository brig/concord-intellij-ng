package brig.concord.inspection.fix;

import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageViewManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShowAllDuplicateFlowsFix implements LocalQuickFix {

    @Override
    public @NotNull String getFamilyName() {
        return "Show all duplicates";
    }

    @Override
    public @NotNull String getName() {
        return "Show all duplicates";
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project,
                                                         @NotNull ProblemDescriptor previewDescriptor) {
        return IntentionPreviewInfo.EMPTY;
    }

    @Override
    public boolean startInWriteAction() {
        // We don't modify PSI, we only show a UI usage view.
        return false;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return;
        }

        var data = ReadAction.compute(() -> {
            var element = descriptor.getPsiElement();
            if (element == null || !element.isValid()) {
                return null;
            }

            var flowName = element.getText();
            if (flowName == null || flowName.isBlank()) {
                return null;
            }

            var process = ProcessDefinitionProvider.getInstance().get(element);
            if (process == null) {
                return null;
            }

            var usages = new ArrayList<Usage>();
            for (var flowDef : process.flows(flowName)) {
                if (flowDef == null || !flowDef.isValid()) {
                    continue;
                }

                var defKv = (flowDef instanceof YAMLKeyValue)
                        ? (YAMLKeyValue) flowDef
                        : PsiTreeUtil.getParentOfType(flowDef, YAMLKeyValue.class, false);

                if (defKv == null) {
                    continue;
                }

                var keyEl = defKv.getKey();
                if (keyEl == null) {
                    continue;
                }

                usages.add(new UsageInfo2UsageAdapter(new UsageInfo(keyEl)));
            }

            if (usages.size() < 2) {
                return null;
            }

            var presentation = new UsageViewPresentation();
            presentation.setTabText("Duplicate flows: " + flowName);
            presentation.setTabName("Duplicate flows: " + flowName);
            presentation.setSearchString("Duplicates");

            return new Object[]{usages, presentation};
        });

        if (data == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        var usages = (List<Usage>) data[0];
        var presentation = (UsageViewPresentation) data[1];

        ApplicationManager.getApplication().invokeLater(() -> {
            UsageViewManager.getInstance(project).showUsages(
                    UsageTarget.EMPTY_ARRAY,
                    usages.toArray(Usage[]::new),
                    presentation
            );
        });
    }
}
