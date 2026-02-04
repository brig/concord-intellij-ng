package brig.concord.psi;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static brig.concord.psi.ConcordFile.isConcordFileName;

/**
 * Service that manages Concord project scopes.
 * A scope is defined by a root concord.yaml file and includes all files that match
 * its resources.concord patterns. Files can belong to multiple scopes if they match
 * patterns from multiple roots.
 */
@Service(Service.Level.PROJECT)
public final class ConcordScopeService {

    private static final Key<CachedValue<List<ConcordRoot>>> ROOTS_CACHE_KEY =
            Key.create("ConcordScopeService.roots");
    private static final Key<CachedValue<Collection<VirtualFile>>> ALL_CONCORD_FILES_CACHE_KEY =
            Key.create("ConcordScopeService.allConcordFiles");
    private static final Key<CachedValue<Map<VirtualFile, Set<VirtualFile>>>> SCOPE_FILES_CACHE_KEY =
            Key.create("ConcordScopeService.scopeFiles");

    private final Project project;
    private Predicate<VirtualFile> ignoredFileChecker;

    public ConcordScopeService(@NotNull Project project) {
        this.project = project;
        this.ignoredFileChecker = file -> {
            if (file.getPath().contains(File.separator + "target" + File.separator)) {
                return true;
            }
            if (FileTypeManager.getInstance().isFileIgnored(file)) {
                return true;
            }
            if (ProjectFileIndex.getInstance(project).isExcluded(file)) {
                return true;
            }
            return ChangeListManager.getInstance(project).isIgnoredFile(file);
        };
    }

    public static @NotNull ConcordScopeService getInstance(@NotNull Project project) {
        return project.getService(ConcordScopeService.class);
    }

    @TestOnly
    public void setIgnoredFileChecker(@NotNull Predicate<VirtualFile> ignoredFileChecker) {
        this.ignoredFileChecker = ignoredFileChecker;
    }

    /**
     * Checks if the file is ignored by the VCS (e.g. .gitignore).
     */
    public boolean isIgnored(@NotNull VirtualFile file) {
        return ignoredFileChecker.test(file);
    }

    public boolean isIgnored(@NotNull PsiFile file) {
        var vf = file.getVirtualFile();
        if (vf == null) {
            return true;
        }
        return isIgnored(vf);
    }

    /**
     * Returns all scopes that contain the given file.
     * Filters out ignored files.
     *
     * @param file the file to check
     * @return list of scopes containing this file (may be empty)
     */
    public @NotNull List<ConcordRoot> getScopesForFile(@NotNull VirtualFile file) {
        var roots = findRoots();
        var result = new ArrayList<ConcordRoot>();

        for (var root : roots) {
            if (isInsideScope(root, file)) {
                result.add(root);
            }
        }

        return result;
    }

    public boolean isOutOfScope(@NotNull VirtualFile file) {
        if (!isConcordFileName(file.getName())) {
            return false;
        }

        if (isRootFile(file)) {
            return false;
        }

        return getScopesForFile(file).isEmpty();
    }

    /**
     * Creates a GlobalSearchScope that includes all files visible from the given context element.
     * This scope includes all files from all scopes that contain the context file.
     * Computed on-the-fly using cached patterns from ConcordRoot.
     * Fast: ~10 roots × ~100 files = ~1000 pattern matches.
     *
     * @param context the PSI element providing context
     * @return a search scope for file-based index queries
     */
    public @NotNull GlobalSearchScope createSearchScope(@NotNull PsiElement context) {
        var psiFile = context.getContainingFile();
        if (psiFile == null) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        var file = psiFile.getOriginalFile().getVirtualFile();
        if (file == null) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        return createSearchScope(file);
    }

    private boolean isInsideScope(@NotNull ConcordRoot scope, @NotNull VirtualFile file) {
        if (file.equals(scope.getRootFile())) {
            return true;
        }

        var scopeFiles = getScopeFilesMap().get(scope.getRootFile());
        return scopeFiles != null && scopeFiles.contains(file);
    }

    /**
     * Creates a GlobalSearchScope that includes all files visible from the given file.
     *
     * @param file the file providing context
     * @return a search scope for file-based index queries
     */
    @NotNull GlobalSearchScope createSearchScope(@NotNull VirtualFile file) {
        var scopes = getScopesForFile(file);

        if (scopes.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        return createScopeFromRoots(scopes);
    }

    /**
     * Finds all Concord root files in the project.
     * A root is a concord.yaml file that is not contained in any other root's scope.
     *
     * @return list of all detected Concord roots
     */
    public @NotNull List<ConcordRoot> findRoots() {
        return CachedValuesManager.getManager(project).getCachedValue(project, ROOTS_CACHE_KEY, () -> {
            var roots = computeRoots();
            return CachedValueProvider.Result.create(
                    roots,
                    ConcordModificationTracker.getInstance(project).structure()
            );
        }, false);
    }

    /**
     * Creates a search scope containing all files from the given roots.
     * Uses cached scope files for each root.
     */
    private @NotNull GlobalSearchScope createScopeFromRoots(@NotNull List<ConcordRoot> roots) {
        if (roots.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        var scopeFilesMap = getScopeFilesMap();
        Set<VirtualFile> files = CollectionFactory.createSmallMemoryFootprintSet();

        for (var root : roots) {
            files.add(root.getRootFile());
            var scopeFiles = scopeFilesMap.get(root.getRootFile());
            if (scopeFiles != null) {
                files.addAll(scopeFiles);
            }
        }

        if (files.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        return GlobalSearchScope.filesScope(project, files);
    }

    /**
     * Returns a cached map from root file to its scope files.
     * Invalidated when structure or patterns change.
     */
    private @NotNull Map<VirtualFile, Set<VirtualFile>> getScopeFilesMap() {
        return CachedValuesManager.getManager(project).getCachedValue(project, SCOPE_FILES_CACHE_KEY, () -> {
            var map = computeScopeFilesMap();
            return CachedValueProvider.Result.create(
                    map,
                    ConcordModificationTracker.getInstance(project).structure()
            );
        }, false);
    }

    /**
     * Computes the scope files map: for each root, which files belong to its scope.
     */
    private @NotNull Map<VirtualFile, Set<VirtualFile>> computeScopeFilesMap() {
        var roots = findRoots();
        var allConcordFiles = findAllConcordFiles();

        if (roots.isEmpty()) {
            return Map.of();
        }

        Map<VirtualFile, Set<VirtualFile>> result = new HashMap<>();

        for (var root : roots) {
            Set<VirtualFile> scopeFiles = allConcordFiles.stream()
                    .filter(root::contains)
                    .collect(Collectors.toSet());
            result.put(root.getRootFile(), scopeFiles);
        }

        return result;
    }

    /**
     * Returns all Concord files in the project (cached).
     * This includes all files with names ending in concord.yml, concord.yaml, etc.
     */
    private @NotNull Collection<VirtualFile> findAllConcordFiles() {
        return CachedValuesManager.getManager(project).getCachedValue(project, ALL_CONCORD_FILES_CACHE_KEY, () -> {
            var files = computeAllConcordFiles();
            return CachedValueProvider.Result.create(
                    files,
                    ConcordModificationTracker.getInstance(project).structure()
            );
        }, false);
    }

    /**
     * Computes all Concord files in the project.
     * Scans FilenameIndex once and filters by Concord naming patterns.
     */
    private @NotNull Collection<VirtualFile> computeAllConcordFiles() {
        Set<String> concordFileNames = CollectionFactory.createSmallMemoryFootprintSet();
        var projectScope = GlobalSearchScope.projectScope(project);

        FilenameIndex.processAllFileNames(name -> {
            if (isConcordFileName(name)) {
                concordFileNames.add(name);
            }
            return true;
        }, projectScope, null);

        if (concordFileNames.isEmpty()) {
            return List.of();
        }

        var result = new ArrayList<VirtualFile>();
        FilenameIndex.processFilesByNames(concordFileNames, true, projectScope, null, file -> {
            result.add(file);
            return true;
        });

        return result.stream()
                .filter(f -> !isIgnored(f))
                .toList();
    }

    /**
     * Computes all Concord roots in the project.
     * Note: ignored files filtering is done at usage time, not here,
     * to ensure fresh VCS status is used.
     */
    private @NotNull List<ConcordRoot> computeRoots() {
        // Step 1: Find all potential root files from all Concord files
        var allConcordFiles = findAllConcordFiles();
        var potentialRoots = allConcordFiles.stream()
                .filter(ConcordScopeService::isRootFile)
                .map(this::createRoot)
                .toList();

        if (potentialRoots.isEmpty()) {
            return List.of();
        }

        // Step 2: Filter out non-roots (files that are contained in another root's scope)
         return potentialRoots.stream()
                 .filter(candidate -> potentialRoots.stream()
                         .noneMatch(other -> !candidate.equals(other) && other.contains(candidate.getRootFile())))
                 .toList();
    }

    private static boolean isRootFile(@NotNull VirtualFile file) {
        return ConcordFile.isRootFileName(file.getName());
    }

    /**
     * Creates a ConcordRoot from a virtual file.
     */
    private @NotNull ConcordRoot createRoot(@NotNull VirtualFile file) {
        return new ConcordRoot(project, file);
    }
}
