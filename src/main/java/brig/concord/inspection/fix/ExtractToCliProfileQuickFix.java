// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection.fix;

import brig.concord.ConcordBundle;
import brig.concord.dependency.MavenCoordinate;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import brig.concord.yaml.YAMLElementGenerator;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class ExtractToCliProfileQuickFix implements LocalQuickFix {

    @SafeFieldForPreview
    private final MavenCoordinate coordinate;

    public ExtractToCliProfileQuickFix(@NotNull MavenCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public @NotNull String getName() {
        return ConcordBundle.message("inspection.server.only.dependency.fix",
                coordinate.toGA());
    }

    @Override
    public @NotNull String getFamilyName() {
        return ConcordBundle.message("inspection.server.only.dependency.fix.family");
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project,
                                                         @NotNull ProblemDescriptor previewDescriptor) {
        return IntentionPreviewInfo.EMPTY;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return;
        }

        var version = Messages.showInputDialog(
                project,
                ConcordBundle.message("inspection.server.only.dependency.fix.dialog.message",
                        coordinate.toGA()),
                ConcordBundle.message("inspection.server.only.dependency.fix.dialog.title"),
                null
        );

        if (version == null || version.isBlank()) {
            return;
        }

        var newCoord = coordinate.withVersion(version.trim());
        var depString = "\"" + newCoord + "\"";

        var file = descriptor.getPsiElement().getContainingFile();
        if (file == null) {
            return;
        }

        var vf = file.getVirtualFile();
        if (vf == null) {
            return;
        }

        var scopes = ConcordScopeService.getInstance(project).getScopesForFile(vf);
        if (scopes.isEmpty()) {
            return;
        }

        var psiManager = PsiManager.getInstance(project);
        for (var scope : scopes) {
            var rootVf = scope.getRootFile();
            var rootPsi = psiManager.findFile(rootVf);
            if (!(rootPsi instanceof ConcordFile rootConcord)) {
                continue;
            }

            WriteCommandAction.runWriteCommandAction(project, getFamilyName(), null, () -> {
                insertDependency(project, rootConcord, depString);
            }, rootPsi);
        }
    }

    private void insertDependency(@NotNull Project project,
                                  @NotNull ConcordFile rootFile,
                                  @NotNull String depString) {
        var generator = YAMLElementGenerator.getInstance(project);

        // Navigate: profiles -> cli -> configuration -> dependencies
        var profilesKv = rootFile.profiles().orElse(null);
        if (profilesKv == null) {
            // Create entire profiles.cli.configuration.dependencies structure
            var yaml = "profiles:\n" +
                    "  cli:\n" +
                    "    configuration:\n" +
                    "      dependencies:\n" +
                    "        - " + depString;
            var dummyFile = generator.createDummyYamlWithText(yaml);
            var dummyKv = PsiTreeUtil.findChildOfType(
                    PsiTreeUtil.findChildOfType(dummyFile, YAMLDocument.class),
                    YAMLKeyValue.class
            );
            if (dummyKv == null) {
                return;
            }

            var doc = rootFile.getDocument().orElse(null);
            if (doc == null) {
                return;
            }
            var topLevel = doc.getTopLevelValue();
            if (topLevel instanceof YAMLMapping topMapping) {
                topMapping.putKeyValue(dummyKv);
            }
            return;
        }

        var profilesValue = profilesKv.getValue();
        if (!(profilesValue instanceof YAMLMapping profilesMapping)) {
            return;
        }

        var cliKv = profilesMapping.getKeyValueByKey("cli");
        if (cliKv == null) {
            var newKv = generator.createYamlKeyValue("cli",
                    "configuration:\n  dependencies:\n    - " + depString);
            profilesMapping.putKeyValue(newKv);
            return;
        }

        var cliValue = cliKv.getValue();
        if (!(cliValue instanceof YAMLMapping cliMapping)) {
            return;
        }

        var configKv = cliMapping.getKeyValueByKey("configuration");
        if (configKv == null) {
            var newKv = generator.createYamlKeyValue("configuration",
                    "dependencies:\n  - " + depString);
            cliMapping.putKeyValue(newKv);
            return;
        }

        var configValue = configKv.getValue();
        if (!(configValue instanceof YAMLMapping configMapping)) {
            return;
        }

        var depsKv = configMapping.getKeyValueByKey("dependencies");
        if (depsKv == null) {
            var newKv = generator.createYamlKeyValue("dependencies",
                    "- " + depString);
            configMapping.putKeyValue(newKv);
            return;
        }

        var depsValue = depsKv.getValue();
        if (depsValue instanceof YAMLSequence depsSeq) {
            var newItem = generator.createSequenceItem(depString);
            depsSeq.addItem(newItem);
        }
    }
}
