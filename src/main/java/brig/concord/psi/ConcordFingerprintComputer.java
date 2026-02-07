package brig.concord.psi;

import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ConcordFingerprintComputer {

    private ConcordFingerprintComputer() {
    }

    public static @Nullable ConcordFileFingerprint compute(@NotNull ConcordFile yamlFile, boolean isRoot) {
        if (PsiTreeUtil.findChildOfType(yamlFile, PsiErrorElement.class) != null) {
            return null;
        }

        var doc = PsiTreeUtil.getChildOfType(yamlFile, YAMLDocument.class);
        if (doc == null) {
            return ConcordFileFingerprint.EMPTY;
        }

        var root = PsiTreeUtil.getChildOfType(doc, YAMLMapping.class);
        if (root == null) {
            return ConcordFileFingerprint.EMPTY;
        }

        var resources = isRoot ? readResources(root) : null;

        var config = readMapping(root, "configuration");
        if (config.state == MappingState.INVALID) {
            return null;
        }

        var dependencies = readList(config.mapping, "dependencies");
        if (dependencies == null) {
            return null;
        }

        var extraDependencies = readList(config.mapping, "extraDependencies");
        if (extraDependencies == null) {
            return null;
        }

        var profiles = readProfiles(root);
        if (profiles == null) {
            return null;
        }

        return new ConcordFileFingerprint(
                normalize(resources),
                normalize(dependencies),
                normalize(extraDependencies),
                profiles
        );
    }

    private static @Nullable List<String> readResources(@NotNull YAMLMapping root) {
        var resources = readMapping(root, "resources");
        if (resources.state == MappingState.ABSENT) {
            return List.of();
        }
        if (resources.state == MappingState.INVALID) {
            return null;
        }

        return readList(resources.mapping, "concord");
    }

    private static @Nullable Map<String, ConcordFileFingerprint.ProfileFingerprint> readProfiles(@NotNull YAMLMapping root) {
        var profilesMap = readMapping(root, "profiles");
        if (profilesMap.state == MappingState.ABSENT) {
            return Map.of();
        }
        if (profilesMap.state == MappingState.INVALID) {
            return null;
        }

        if (profilesMap.mapping == null) {
            return null;
        }

        Map<String, ConcordFileFingerprint.ProfileFingerprint> result = new HashMap<>();

        for (var kv : profilesMap.mapping.getKeyValues()) {
            var profileName = kv.getKeyText().trim();
            var profileValue = kv.getValue();
            if (!(profileValue instanceof YAMLMapping profileMap)) {
                return null;
            }

            var configMap = readMapping(profileMap, "configuration");
            if (configMap.state == MappingState.INVALID) {
                return null;
            }

            var dependencies = readList(configMap.mapping, "dependencies");
            if (dependencies == null) {
                return null;
            }

            var extraDependencies = readList(configMap.mapping, "extraDependencies");
            if (extraDependencies == null) {
                return null;
            }

            result.put(
                    profileName,
                    new ConcordFileFingerprint.ProfileFingerprint(
                            normalize(dependencies),
                            normalize(extraDependencies)
                    )
            );
        }

        return Map.copyOf(result);
    }

    private enum MappingState {
        ABSENT,
        PRESENT,
        INVALID
    }

    private record MappingResult(MappingState state, @Nullable YAMLMapping mapping) {
        static MappingResult absent() {
            return new MappingResult(MappingState.ABSENT, null);
        }

        static MappingResult invalid() {
            return new MappingResult(MappingState.INVALID, null);
        }

        static MappingResult present(@NotNull YAMLMapping mapping) {
            return new MappingResult(MappingState.PRESENT, mapping);
        }
    }

    private static @NotNull MappingResult readMapping(@NotNull YAMLMapping parent, @NotNull String key) {
        var kv = parent.getKeyValueByKey(key);
        if (kv == null) {
            return MappingResult.absent();
        }

        PsiElement value = kv.getValue();
        if (value == null) {
            return MappingResult.invalid();
        }
        if (!(value instanceof YAMLMapping mapping)) {
            return MappingResult.invalid();
        }
        return MappingResult.present(mapping);
    }

    private static @Nullable List<String> readList(@Nullable YAMLMapping mapping, @NotNull String key) {
        if (mapping == null) {
            return List.of();
        }

        var kv = mapping.getKeyValueByKey(key);
        if (kv == null) {
            return List.of();
        }

        PsiElement value = kv.getValue();
        switch (value) {
            case null -> {
                return null;
            }
            case YAMLSequence sequence -> {
                List<String> result = new ArrayList<>();
                for (var item : sequence.getItems()) {
                    PsiElement itemValue = item.getValue();
                    if (!(itemValue instanceof YAMLScalar scalar)) {
                        return null;
                    }
                    var text = scalar.getTextValue();
                    if (!text.isBlank()) {
                        result.add(text.trim());
                    }
                }
                return result;
            }
            case YAMLScalar scalar -> {
                var text = scalar.getTextValue();
                if (text.isBlank()) {
                    return List.of();
                }
                return List.of(text.trim());
            }
            default -> {
            }
        }

        return null;
    }

    private static List<String> normalize(@Nullable List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();
        for (var value : values) {
            if (value == null) {
                continue;
            }
            var trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }

        if (normalized.isEmpty()) {
            return List.of();
        }

        Collections.sort(normalized);
        List<String> deduped = new ArrayList<>(normalized.size());
        String previous = null;
        for (var item : normalized) {
            if (!item.equals(previous)) {
                deduped.add(item);
                previous = item;
            }
        }

        return List.copyOf(deduped);
    }
}
