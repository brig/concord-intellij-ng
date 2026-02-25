// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record RegistryState(
        Map<VirtualFile, Map<String, TaskInfo>> taskInfosByScope,
        Map<MavenCoordinate, String> failedDependencies,
        Set<MavenCoordinate> skippedDependencies,
        Set<MavenCoordinate> resolvedDependencies,
        Set<VirtualFile> problemFiles
) {
    public static final RegistryState EMPTY = new RegistryState(Map.of(), Map.of(), Set.of(), Set.of(), Set.of());

    public RegistryState {
        taskInfosByScope = taskInfosByScope.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> Map.copyOf(e.getValue())));
        failedDependencies = Map.copyOf(failedDependencies);
        skippedDependencies = Set.copyOf(skippedDependencies);
        resolvedDependencies = Set.copyOf(resolvedDependencies);
        problemFiles = Set.copyOf(problemFiles);
    }

    public Map<String, TaskInfo> getTasksForScope(VirtualFile scopeRoot) {
        return taskInfosByScope.getOrDefault(scopeRoot, Map.of());
    }
}
