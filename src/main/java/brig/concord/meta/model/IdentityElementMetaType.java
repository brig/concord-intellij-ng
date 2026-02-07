package brig.concord.meta.model;

import brig.concord.psi.YamlPsiUtils;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;

import java.util.*;

public abstract class IdentityElementMetaType extends YamlAnyOfType {

    private final List<IdentityMetaType> entries;

    protected IdentityElementMetaType(String typeName, List<IdentityMetaType> entries) {
        super(typeName, List.copyOf(entries));

        this.entries = entries;
    }

    public IdentityMetaType findEntry(YAMLMapping element) {
        return findEntry(YamlPsiUtils.keys(element));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        var meta = findEntry(Set.of(name));
        if (meta == null) {
            return null;
        }

        return new Field(name, this) {

            @Override
            public @NotNull Field resolveToSpecializedField(@NotNull YAMLValue element) {
                var m = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, false);
                if (m == null) {
                    return this;
                }

                var meta = findEntry(YamlPsiUtils.keys(m));
                if (meta == null) {
                    return this;
                }

                var kv = YamlPsiUtils.getParentOfType(element, YAMLKeyValue.class, false);
                if (kv == null) {
                    return this;
                }

                var field = meta.findFeatureByName(kv.getKeyText());
                if (field != null) {
                    return new Field(name, field.resolveToSpecializedField(element).getDefaultType());
                }

                return this;
            }
        };
    }

    @Override
    public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
        var stepMeta = identifyEntry(existingFields);
        if (stepMeta == null) {
            return Collections.emptyList();
        }
        return stepMeta.computeMissingFields(existingFields);
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        var meta = Optional.ofNullable(existingMapping)
                .map(YamlPsiUtils::keys)
                .map(this::identifyEntry)
                .orElse(null);

        if (meta != null) {
            return meta.computeKeyCompletions(existingMapping);
        }

        Set<String> processedNames = new HashSet<>();
        var result = new LinkedHashSet<Field>();
        for (var e : entries) {
            var identity = e.getIdentity();
            if (!processedNames.contains(identity)) {
                processedNames.add(identity);
                result.add(e.findFeatureByName(identity));
            }
        }
        return new LinkedList<>(result);
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (!(value instanceof YAMLMapping m)) {
            return;
        }

        var meta = findEntry(YamlPsiUtils.keys(m));
        if (meta == null) {
            return;
        }

        meta.validateValue(value, problemsHolder);
    }

    protected IdentityMetaType identifyEntry(Set<String> existingKeys) {
        for (var step : entries) {
            if (existingKeys.contains(step.getIdentity())) {
                return step;
            }
        }

        return null;
    }

    private IdentityMetaType findEntry(Set<String> existingKeys) {
        var result = identifyEntry(existingKeys);
        if (result != null) {
            return result;
        }

        var maxMatches = 0;
        var existingKeysSize = existingKeys.size();
        for (var s : entries) {
            var features = s.getFeatures().keySet();
            var matches = 0;
            for (var k : existingKeys) {
                if (features.contains(k)) {
                    matches++;
                }
            }
            if (matches > maxMatches) {
                maxMatches = matches;
                result = s;
                if (maxMatches == existingKeysSize) {
                    return result;
                }
            }
        }

        return result;
    }
}
