package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.psi.YamlPsiUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

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

    public @Nullable Field findFeatureByName(@NotNull PsiElement element, @NotNull String name) {
        if (!(element instanceof YAMLMapping m)) {
            return null;
        }

        IdentityMetaType meta = findEntry(YamlPsiUtils.keys(m));
        if (meta == null) {
            return null;
        }

        Field field = meta.findFeatureByName(name);
        if (field != null) {
            return new Field(name, field.resolveToSpecializedField(m).getDefaultType());
        }

        return null;
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        IdentityMetaType meta = findEntry(Set.of(name));
        if (meta == null) {
            return null;
        }

        return new Field(name, this) {

            @Override
            public @NotNull Field resolveToSpecializedField(@NotNull YAMLValue element) {
                YAMLMapping m = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, false);
                if (m == null) {
                    return this;
                }

                IdentityMetaType meta = findEntry(YamlPsiUtils.keys(m));
                if (meta == null) {
                    return this;
                }

                YAMLKeyValue kv = YamlPsiUtils.getParentOfType(element, YAMLKeyValue.class, false);
                if (kv == null) {
                    return this;
                }

                Field field = meta.findFeatureByName(kv.getKeyText());
                if (field != null) {
                    return new Field(name, field.resolveToSpecializedField(element).getDefaultType());
                }

                return this;
            }
        };
    }

    @Override
    public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
        IdentityMetaType stepMeta = identifyEntry(existingFields);
        if (stepMeta == null) {
            return Collections.emptyList();
        }
        return stepMeta.computeMissingFields(existingFields);
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        IdentityMetaType meta = Optional.ofNullable(existingMapping)
                        .map(YamlPsiUtils::keys)
                        .map(this::identifyEntry)
                .orElse(null);

        if (meta != null) {
            return meta.computeKeyCompletions(existingMapping);
        }

        Set<String> processedNames = new HashSet<>();
        LinkedHashSet<Field> result = new LinkedHashSet<>();
        for (IdentityMetaType e : entries) {
            String identity = e.getIdentity();
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

        IdentityMetaType meta = findEntry(YamlPsiUtils.keys(m));
        if (meta == null) {
            return;
        }

        meta.validateValue(value, problemsHolder);
    }

    private IdentityMetaType identifyEntry(Set<String> existingKeys) {
        for (IdentityMetaType step : entries) {
            if (existingKeys.contains(step.getIdentity())) {
                return step;
            }
        }

        return null;
    }

    private IdentityMetaType findEntry(Set<String> existingKeys) {
        IdentityMetaType result = identifyEntry(existingKeys);
        if (result != null) {
            return result;
        }

        int maxMatches = 0;
        for (IdentityMetaType s : entries) {
            int matches = 0;
            Set<String> features = s.getFeatures().keySet();
            for (String k : existingKeys) {
                if (features.contains(k)) {
                    matches++;
                }
            }
            if (matches > maxMatches) {
                maxMatches = matches;
                result = s;
            }
        }

        return result;
    }
}
