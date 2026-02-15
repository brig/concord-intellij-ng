package brig.concord.psi;

import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Service(Service.Level.PROJECT)
public final class ArgumentsCollector {

    private static final Key<CachedValue<Map<VirtualFile, Map<String, YAMLValue>>>> CACHE_KEY =
            Key.create("ArgumentsCollector.byScope");

    private final Project project;

    public ArgumentsCollector(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull ArgumentsCollector getInstance(@NotNull Project project) {
        return project.getService(ArgumentsCollector.class);
    }

    /**
     * Returns merged arguments for the element's scope.
     * Finds all scopes containing the element's file and merges arguments from all of them.
     */
    public @NotNull Map<String, YAMLValue> getArguments(@NotNull PsiElement context) {
        var psiFile = context.getContainingFile();
        if (psiFile == null) {
            return Map.of();
        }

        var vf = psiFile.getOriginalFile().getVirtualFile();
        if (vf == null) {
            return Map.of();
        }

        var scopeService = ConcordScopeService.getInstance(project);
        var scopes = scopeService.getScopesForFile(vf);
        if (scopes.isEmpty()) {
            return Map.of();
        }

        var byScope = collectByScope();
        Map<String, YAMLValue> merged = new LinkedHashMap<>();
        for (var scope : scopes) {
            var scopeArgs = byScope.get(scope.getRootFile());
            if (scopeArgs != null) {
                merged.putAll(scopeArgs);
            }
        }

        return merged;
    }

    /**
     * Returns arguments grouped by scope root (cached).
     */
    public @NotNull Map<VirtualFile, Map<String, YAMLValue>> collectByScope() {
        return CachedValuesManager.getManager(project).getCachedValue(project, CACHE_KEY, () -> {
            var result = doCollectByScope();
            var tracker = ConcordModificationTracker.getInstance(project);
            return CachedValueProvider.Result.create(result, tracker.structure(), tracker.arguments());
        }, false);
    }

    private @NotNull Map<VirtualFile, Map<String, YAMLValue>> doCollectByScope() {
        var scopeService = ConcordScopeService.getInstance(project);
        var roots = scopeService.findRoots();

        if (roots.isEmpty()) {
            return Map.of();
        }

        Map<VirtualFile, Map<String, YAMLValue>> result = new HashMap<>();
        for (var root : roots) {
            var args = collectForScope(root);
            if (!args.isEmpty()) {
                result.put(root.getRootFile(), args);
            }
        }

        return result;
    }

    /**
     * Collects merged arguments for a single scope.
     * Files are sorted by name; root file is processed last (overrides everything).
     */
    private @NotNull Map<String, YAMLValue> collectForScope(@NotNull ConcordRoot root) {
        var scopeService = ConcordScopeService.getInstance(project);
        var filesInScope = scopeService.getFilesInScope(root);
        var rootFile = root.getRootFile();

        List<VirtualFile> sortedFiles = new ArrayList<>(filesInScope);
        sortedFiles.remove(rootFile);
        sortedFiles.sort(Comparator.comparing(VirtualFile::getName));

        Map<String, YAMLValue> result = new LinkedHashMap<>();
        var psiManager = PsiManager.getInstance(project);

        for (var vf : sortedFiles) {
            var psiFile = psiManager.findFile(vf);
            if (psiFile instanceof ConcordFile concordFile) {
                collectArguments(concordFile, result);
            }
        }

        // Root file processed last — overrides everything
        var rootPsiFile = psiManager.findFile(rootFile);
        if (rootPsiFile instanceof ConcordFile concordFile) {
            collectArguments(concordFile, result);
        }

        return result;
    }

    /**
     * Extracts arguments from a single ConcordFile into the target map.
     */
    private static void collectArguments(@NotNull ConcordFile file, @NotNull Map<String, YAMLValue> target) {
        file.configuration().ifPresent(configKv -> {
            var configValue = configKv.getValue();
            if (!(configValue instanceof YAMLMapping configMapping)) {
                return;
            }

            var argsKv = configMapping.getKeyValueByKey("arguments");
            if (argsKv == null) {
                return;
            }

            var argsValue = argsKv.getValue();
            if (!(argsValue instanceof YAMLMapping argsMapping)) {
                return;
            }

            for (var kv : argsMapping.getKeyValues()) {
                var key = kv.getKeyText().trim();
                var value = kv.getValue();
                if (!key.isEmpty() && value != null) {
                    target.put(key, value);
                }
            }
        });
    }
}
