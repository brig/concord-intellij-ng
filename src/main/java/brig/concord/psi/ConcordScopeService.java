package brig.concord.psi;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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

import java.util.*;
import java.util.function.Predicate;

import static brig.concord.psi.ConcordFile.isConcordFileName;

/**
 * Service that manages Concord project scopes.
 * A scope is defined by a root concord.yaml file and includes all files that match
 * its resources.concord patterns. Files can belong to multiple scopes if they match
 * patterns from multiple roots.
 */
@Service(Service.Level.PROJECT)
public final class ConcordScopeService {

    private static final Logger LOG = Logger.getInstance(ConcordScopeService.class);

    private static final Key<CachedValue<List<ConcordRoot>>> ROOTS_CACHE_KEY =
            Key.create("ConcordScopeService.roots");
    private static final Key<CachedValue<Map<String, Set<VirtualFile>>>> MATCHING_FILES_CACHE_KEY =
            Key.create("ConcordScopeService.matchingFiles");
    private static final Key<CachedValue<Collection<VirtualFile>>> ALL_CONCORD_FILES_CACHE_KEY =
            Key.create("ConcordScopeService.allConcordFiles");
    private static final Key<CachedValue<GlobalSearchScope>> SEARCH_SCOPE_CACHE_KEY =
            Key.create("ConcordScopeService.searchScope");

    private final Project project;
    private Predicate<VirtualFile> ignoredFileChecker;

    public ConcordScopeService(@NotNull Project project) {
        this.project = project;
        this.ignoredFileChecker = file -> ChangeListManager.getInstance(project).isIgnoredFile(file);
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
        var result = ignoredFileChecker.test(file);

//        LOG.warn("isIgnored(file=" + file + "-> " + result);

        return result;
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
     * Filters out ignored files and roots at this point to ensure fresh VCS status.
     *
     * @param file the file to check
     * @return list of scopes containing this file (may be empty)
     */
    public @NotNull List<ConcordRoot> getScopesForFile(@NotNull VirtualFile file) {
        if (isIgnored(file)) {
            return List.of();
        }

        var roots = findRoots();
        List<ConcordRoot> result = new ArrayList<>();

        for (var root : roots) {
            // Skip ignored root files
            if (isIgnored(root.getRootFile())) {
                continue;
            }
            if (root.contains(file)) {
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
     *
     * @param context the PSI element providing context
     * @return a search scope for file-based index queries
     */
    public @NotNull GlobalSearchScope createSearchScope(@NotNull PsiElement context) {
        var psiFile = context.getContainingFile();
        if (psiFile == null) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        return CachedValuesManager.getCachedValue(psiFile, SEARCH_SCOPE_CACHE_KEY, () -> {
            var file = psiFile.getOriginalFile().getVirtualFile();
            GlobalSearchScope scope;
            if (file == null) {
                scope = GlobalSearchScope.EMPTY_SCOPE;
            } else {
                scope = createSearchScope(file);
            }

            return CachedValueProvider.Result.create(
                    scope,
                    ConcordModificationTracker.tracker(project)
            );
        });
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
            // File is not in any scope - check if it's a potential root itself
            if (isRootFile(file)) {
                // It's a root file - create scope from its patterns
                var tempRoot = createRoot(file);
                return createScopeFromRoots(Collections.singletonList(tempRoot));
            }
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
    @NotNull List<ConcordRoot> findRoots() {
        return CachedValuesManager.getManager(project).getCachedValue(project, ROOTS_CACHE_KEY, () -> {
            var roots = computeRoots();
            return CachedValueProvider.Result.create(
                    roots,
                    ConcordModificationTracker.tracker(project)
            );
        }, false);
    }

    /**
     * Creates a search scope containing all files from the given roots.
     * Filters out ignored files at this point to ensure fresh VCS status is used.
     */
    private @NotNull GlobalSearchScope createScopeFromRoots(@NotNull List<ConcordRoot> roots) {
        Set<VirtualFile> files = CollectionFactory.createSmallMemoryFootprintSet();

        for (var root : roots) {
            var rootFile = root.getRootFile();
            if (!isIgnored(rootFile)) {
                files.add(rootFile);
            }
            for (var file : getMatchingFilesForRoot(root)) {
                if (!isIgnored(file)) {
                    files.add(file);
                }
            }
        }

        if (files.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        return GlobalSearchScope.filesScope(project, files);
    }

    /**
     * Returns cached matching files for the given root.
     * Uses cached map with VFS modification tracking.
     */
    private @NotNull Set<VirtualFile> getMatchingFilesForRoot(@NotNull ConcordRoot root) {
        var allMatchingFiles = getAllMatchingFiles();
        var result = allMatchingFiles.get(root.getRootFile().getPath());
        return result != null ? result : Set.of();
    }

    /**
     * Returns cached map of all matching files for all roots.
     * The entire map is cached and invalidated together on VFS changes.
     */
    private @NotNull Map<String, Set<VirtualFile>> getAllMatchingFiles() {
        return CachedValuesManager.getManager(project).getCachedValue(project, MATCHING_FILES_CACHE_KEY, () -> {
            var result = computeAllMatchingFiles();
            return CachedValueProvider.Result.create(
                    result,
                    ConcordModificationTracker.tracker(project)
            );
        }, false);
    }

    /**
     * Computes matching files for all roots at once.
     */
    private @NotNull Map<String, Set<VirtualFile>> computeAllMatchingFiles() {
        var roots = findRoots();
        if (roots.isEmpty()) {
            return Map.of();
        }

        var allConcordFiles = findAllConcordFiles();
        Map<String, Set<VirtualFile>> result = new HashMap<>();

        for (var root : roots) {
            Set<VirtualFile> matchingFiles = CollectionFactory.createSmallMemoryFootprintSet();
            for (var file : allConcordFiles) {
                if (root.contains(file)) {
                    matchingFiles.add(file);
                }
            }
            result.put(root.getRootFile().getPath(), matchingFiles);
        }

        return result;
    }

    /**
     * Returns all Concord files in the project (cached).
     * This includes all files with names ending in concord.yml, concord.yaml, etc.
     */
    private @NotNull Collection<VirtualFile> findAllConcordFiles() {
        return CachedValuesManager.getManager(project).getCachedValue(project, ALL_CONCORD_FILES_CACHE_KEY,
                this::computeAllConcordFiles, false);
    }

    /**
     * Computes all Concord files in the project.
     * Scans FilenameIndex once and filters by Concord naming patterns.
     */
    private @NotNull CachedValueProvider.Result<Collection<VirtualFile>> computeAllConcordFiles() {
        Collection<VirtualFile> files = ReadAction.compute(() -> {
            // First pass: collect all file names that match Concord patterns
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

            // Second pass: get actual files (without ignored filtering - that's done at usage time)
            List<VirtualFile> result = new ArrayList<>();
            FilenameIndex.processFilesByNames(concordFileNames, false, projectScope, null, file -> {
                result.add(file);
                return true;
            });

            return result;
        });

        return CachedValueProvider.Result.create(
                files,
                ConcordModificationTracker.tracker(project)
        );
    }

    /**
     * Computes all Concord roots in the project.
     * Note: ignored files filtering is done at usage time, not here,
     * to ensure fresh VCS status is used.
     */
    private @NotNull List<ConcordRoot> computeRoots() {
        return ReadAction.compute(() -> {
            // Step 1: Find all potential root files from cached Concord files
            var allConcordFiles = findAllConcordFiles();
            List<VirtualFile> potentialRoots = new ArrayList<>();

            for (var file : allConcordFiles) {
                if (isRootFile(file)) {
                    potentialRoots.add(file);
                }
            }

            if (potentialRoots.isEmpty()) {
                return List.of();
            }

            // Step 2: Create ConcordRoot for each potential root
            var allRoots = new ArrayList<ConcordRoot>(potentialRoots.size());
            for (var file : potentialRoots) {
                allRoots.add(createRoot(file));
            }

            // Step 3: Filter out non-roots (files that are contained in another root's scope)
            List<ConcordRoot> actualRoots = new ArrayList<>();
            for (var candidate : allRoots) {
                var isContainedInOther = false;

                for (var other : allRoots) {
                    if (candidate.equals(other)) {
                        continue;
                    }

                    // Ignore roots that are ignored by VCS
                    if (isIgnored(other.getRootFile())) {
                        continue;
                    }

                    // Check if candidate's root file is contained in other's scope
                    if (other.contains(candidate.getRootFile())) {
                        isContainedInOther = true;
                        break;
                    }
                }

                if (!isContainedInOther) {
                    actualRoots.add(candidate);
                }
            }

            return Collections.unmodifiableList(actualRoots);
        });
    }

    /**
     * Checks if the given file is a root file (concord.yaml, .concord.yaml, etc.)
     */
    private boolean isRootFile(@NotNull VirtualFile file) {
        return ConcordFile.isRootFileName(file.getName());
    }

    /**
     * Creates a ConcordRoot from a virtual file.
     */
    private @NotNull ConcordRoot createRoot(@NotNull VirtualFile file) {
        return new ConcordRoot(project, file);
    }
}
