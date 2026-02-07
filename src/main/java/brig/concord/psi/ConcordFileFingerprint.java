package brig.concord.psi;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ConcordFileFingerprint(
        List<String> resourcePatterns,
        List<String> dependencies,
        List<String> extraDependencies,
        Map<String, ProfileFingerprint> profiles
) {
    public static final ConcordFileFingerprint EMPTY = new ConcordFileFingerprint(List.of(), List.of(), List.of(), Map.of());

    public boolean hasDependencies() {
        if (!dependencies.isEmpty() || !extraDependencies.isEmpty()) {
            return true;
        }
        for (var profile : profiles.values()) {
            if (profile.hasDependencies()) {
                return true;
            }
        }
        return false;
    }

    public boolean dependenciesEquals(ConcordFileFingerprint other) {
        if (other == null) {
            return false;
        }

        return Objects.equals(dependencies, other.dependencies) &&
                Objects.equals(extraDependencies, other.extraDependencies) &&
                Objects.equals(profiles, other.profiles);
    }

    public record ProfileFingerprint(
            List<String> dependencies,
            List<String> extraDependencies
    ) {
        public boolean hasDependencies() {
            return !dependencies.isEmpty() || !extraDependencies.isEmpty();
        }
    }
}
