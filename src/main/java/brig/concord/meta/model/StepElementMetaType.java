package brig.concord.meta.model;

import brig.concord.psi.YamlPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.*;

public class StepElementMetaType extends YamlAnyOfType {

    private static final List<StepMetaType> steps = List.of(
            TaskStepMetaType.getInstance(),
            CallStepMetaType.getInstance(),
            LogStepMetaType.getInstance(),
            IfStepMetaType.getInstance(),
            ReturnStepMetaType.getInstance(),
            ExitStepMetaType.getInstance(),
            CheckpointStepMetaType.getInstance()
    );

    private static final StepElementMetaType INSTANCE = new StepElementMetaType();

    public static StepElementMetaType getInstance() {
        return INSTANCE;
    }

    protected StepElementMetaType() {
        super("Steps", List.copyOf(steps));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        StepMetaType meta = findStepMeta(Set.of(name));
        if (meta == null) {
            return null;
        }

        return new Field(name, new Field.MetaTypeSupplier() {

            @Override
            public @NotNull YamlMetaType getMainType() {
                return StepElementMetaType.getInstance();
            }

            @Override
            public @Nullable YamlMetaType getSpecializedType(@NotNull YAMLValue element) {
                YAMLMapping m = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, false);
                if (m == null) {
                    return null;
                }

                StepMetaType stepMeta = findStepMeta(YamlPsiUtils.keys(m));
                if (stepMeta == null) {
                    return null;
                }

                YAMLKeyValue kv = YamlPsiUtils.getParentOfType(element, YAMLKeyValue.class, false);
                if (kv == null) {
                    return null;
                }

                Field field = stepMeta.findFeatureByName(kv.getKeyText());
                if (field != null) {
                    return field.getDefaultType();
                }

                return null;
            }
        });
    }

    @Override
    public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
        StepMetaType stepMeta = identifyStepMeta(existingFields);
        if (stepMeta == null) {
            return Collections.emptyList();
        }
        return stepMeta.computeMissingFields(existingFields);
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        StepMetaType stepMeta = Optional.ofNullable(existingMapping)
                        .map(YamlPsiUtils::keys)
                        .map(this::identifyStepMeta)
                .orElse(null);

        if (stepMeta != null) {
            return stepMeta.computeKeyCompletions(existingMapping);
        }

        Set<String> processedNames = new HashSet<>();
        LinkedHashSet<Field> result = new LinkedHashSet<>();
        for (StepMetaType step : steps) {
            String identity = step.getIdentity();
            if (!processedNames.contains(identity)) {
                processedNames.add(identity);
                result.add(step.findFeatureByName(identity));
            }
        }
        return new LinkedList<>(result);
    }

    private StepMetaType identifyStepMeta(Set<String> existingKeys) {
        for (StepMetaType step : steps) {
            if (existingKeys.contains(step.getIdentity())) {
                return step;
            }
        }

        return null;
    }

    private StepMetaType findStepMeta(Set<String> existingKeys) {
        StepMetaType result = identifyStepMeta(existingKeys);
        if (result != null) {
            return result;
        }

        int maxMatches = 0;
        for (StepMetaType s : steps) {
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
