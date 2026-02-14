package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.dependency.DependencyCollector;
import brig.concord.dependency.MavenCoordinate;
import brig.concord.dependency.TaskRegistry;
import brig.concord.inspection.fix.ExtractToIdeaProfileQuickFix;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class ServerOnlyDependencyInspection extends ConcordInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildConcordVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                if (!(file instanceof ConcordFile concordFile)) {
                    return;
                }

                var skippedDeps = TaskRegistry.getInstance(holder.getProject()).getSkippedDependencies();
                if (skippedDeps.isEmpty()) {
                    return;
                }

                var coveredGAs = collectIdeaProfileGAs(concordFile);

                DependencyCollector.forEachDependencyScalar(concordFile, scalar -> {
                    var coordinate = MavenCoordinate.parse(scalar.getTextValue());
                    if (coordinate == null) {
                        return;
                    }

                    if (!skippedDeps.contains(coordinate)) {
                        return;
                    }

                    if (coveredGAs != null && coveredGAs.contains(coordinate.toGA())) {
                        return;
                    }

                    holder.registerProblem(
                            scalar,
                            ConcordBundle.message("inspection.server.only.dependency.message",
                                    coordinate.getVersion()),
                            ProblemHighlightType.WEAK_WARNING,
                            new ExtractToIdeaProfileQuickFix(coordinate)
                    );
                });
            }
        };
    }

    /**
     * Collects groupId:artifactId strings from profiles.idea.configuration.dependencies
     * and profiles.idea.configuration.extraDependencies in the root file of each scope
     * that contains the given file. Returns null if no scopes found.
     */
    private @Nullable Set<String> collectIdeaProfileGAs(@NotNull ConcordFile file) {
        var vf = file.getVirtualFile();
        if (vf == null) {
            return null;
        }

        var project = file.getProject();
        var scopes = ConcordScopeService.getInstance(project).getScopesForFile(vf);
        if (scopes.isEmpty()) {
            return null;
        }

        Set<String> result = new LinkedHashSet<>();
        var psiManager = PsiManager.getInstance(project);

        for (var scope : scopes) {
            var rootFile = scope.getRootFile();
            var rootPsi = psiManager.findFile(rootFile);
            if (!(rootPsi instanceof ConcordFile rootConcord)) {
                continue;
            }

            collectGAsFromIdeaProfile(rootConcord, result);
        }

        return result;
    }

    private void collectGAsFromIdeaProfile(@NotNull ConcordFile rootFile, @NotNull Set<String> result) {
        rootFile.profiles().ifPresent(profilesKv -> {
            var profilesValue = profilesKv.getValue();
            if (!(profilesValue instanceof YAMLMapping profilesMapping)) {
                return;
            }

            var ideaKv = profilesMapping.getKeyValueByKey("idea");
            if (ideaKv == null) {
                return;
            }

            var ideaValue = ideaKv.getValue();
            if (!(ideaValue instanceof YAMLMapping ideaMapping)) {
                return;
            }

            var configKv = ideaMapping.getKeyValueByKey("configuration");
            if (configKv == null) {
                return;
            }

            var configValue = configKv.getValue();
            if (!(configValue instanceof YAMLMapping configMapping)) {
                return;
            }

            DependencyCollector.visitDependencyScalars(configMapping, scalar -> {
                var coord = MavenCoordinate.parse(scalar.getTextValue());
                if (coord != null) {
                    result.add(coord.toGA());
                }
            });
        });
    }
}
