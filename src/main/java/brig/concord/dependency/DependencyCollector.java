package brig.concord.dependency;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordModificationTracker;
import brig.concord.psi.ConcordRoot;
import brig.concord.psi.ConcordScopeService;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects Maven dependencies from Concord files in a project.
 * Extracts dependencies from:
 * - configuration.dependencies
 * - configuration.extraDependencies
 * - profiles.*.configuration.dependencies
 * - profiles.*.configuration.extraDependencies
 */
@Service(Service.Level.PROJECT)
public final class DependencyCollector {

    private static final Key<CachedValue<List<ScopeDependencies>>> CACHE_KEY =
            Key.create("DependencyCollector.byScope");

    private final Project project;

    public DependencyCollector(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull DependencyCollector getInstance(@NotNull Project project) {
        return project.getService(DependencyCollector.class);
    }

    /**
     * A single dependency occurrence with its source location.
     */
    public record DependencyOccurrence(
            @NotNull MavenCoordinate coordinate,
            @NotNull VirtualFile file,
            int textOffset
    ) {}

    /**
     * Result of dependency collection for a single scope.
     */
    public record ScopeDependencies(
            @NotNull ConcordRoot root,
            @NotNull List<DependencyOccurrence> occurrences
    ) {
        public boolean isEmpty() {
            return occurrences.isEmpty();
        }
    }

    /**
     * Collects all unique Maven dependencies from all scopes in the project.
     */
    public @NotNull Set<MavenCoordinate> collectAll() {
        Set<MavenCoordinate> result = new LinkedHashSet<>();
        for (var scopeDeps : collectByScope()) {
            for (var occ : scopeDeps.occurrences()) {
                result.add(occ.coordinate());
            }
        }
        return result;
    }

    /**
     * Collects dependencies grouped by scope (ConcordRoot).
     * Results are cached and invalidated when project structure or dependencies change.
     */
    public @NotNull List<ScopeDependencies> collectByScope() {
        return CachedValuesManager.getManager(project).getCachedValue(project, CACHE_KEY, () -> {
            var result = doCollectByScope();
            var tracker = ConcordModificationTracker.getInstance(project);
            return CachedValueProvider.Result.create(result, tracker.structure(), tracker.dependencies());
        }, false);
    }

    private @NotNull List<ScopeDependencies> doCollectByScope() {
        List<ScopeDependencies> result = new ArrayList<>();
        var roots = ConcordScopeService.getInstance(project).findRoots();

        for (var root : roots) {
            result.add(collectForScope(root));
        }

        return result;
    }

    private @NotNull ScopeDependencies collectForScope(@NotNull ConcordRoot root) {
        List<DependencyOccurrence> occurrences = new ArrayList<>();

        var scopeService = ConcordScopeService.getInstance(project);
        var filesInScope = scopeService.getFilesInScope(root);

        for (var virtualFile : filesInScope) {
            var psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (!(psiFile instanceof ConcordFile concordFile)) {
                continue;
            }

            collectFromConfiguration(concordFile, virtualFile, occurrences);
            collectFromProfiles(concordFile, virtualFile, occurrences);
        }

        return new ScopeDependencies(root, occurrences);
    }

    private void collectFromConfiguration(@NotNull ConcordFile file,
                                          @NotNull VirtualFile virtualFile,
                                          @NotNull List<DependencyOccurrence> target) {
        file.configuration().ifPresent(configKv -> {
            var configValue = configKv.getValue();
            if (configValue instanceof YAMLMapping configMapping) {
                extractDependencies(configMapping, virtualFile, target);
            }
        });
    }

    private void collectFromProfiles(@NotNull ConcordFile file,
                                     @NotNull VirtualFile virtualFile,
                                     @NotNull List<DependencyOccurrence> target) {
        file.profiles().ifPresent(profilesKv -> {
            var profilesValue = profilesKv.getValue();
            if (!(profilesValue instanceof YAMLMapping profilesMapping)) {
                return;
            }

            for (var profileKv : profilesMapping.getKeyValues()) {
                var profileValue = profileKv.getValue();
                if (!(profileValue instanceof YAMLMapping profileMapping)) {
                    continue;
                }

                var configKv = profileMapping.getKeyValueByKey("configuration");
                if (configKv == null) {
                    continue;
                }

                var configValue = configKv.getValue();
                if (configValue instanceof YAMLMapping configMapping) {
                    extractDependencies(configMapping, virtualFile, target);
                }
            }
        });
    }

    private void extractDependencies(@NotNull YAMLMapping configMapping,
                                     @NotNull VirtualFile virtualFile,
                                     @NotNull List<DependencyOccurrence> target) {
        extractDependenciesFromKey(configMapping, "dependencies", virtualFile, target);
        extractDependenciesFromKey(configMapping, "extraDependencies", virtualFile, target);
    }

    private void extractDependenciesFromKey(@NotNull YAMLMapping configMapping,
                                            @NotNull String key,
                                            @NotNull VirtualFile virtualFile,
                                            @NotNull List<DependencyOccurrence> target) {
        var depsKv = configMapping.getKeyValueByKey(key);
        if (depsKv == null) {
            return;
        }

        var depsValue = depsKv.getValue();
        if (!(depsValue instanceof YAMLSequence depsSeq)) {
            return;
        }

        for (var item : depsSeq.getItems()) {
            var itemValue = item.getValue();
            if (itemValue instanceof YAMLScalar scalar) {
                var depString = scalar.getTextValue();
                var coordinate = MavenCoordinate.parse(depString);
                if (coordinate != null) {
                    target.add(new DependencyOccurrence(coordinate, virtualFile, scalar.getTextOffset()));
                }
            }
        }
    }
}
