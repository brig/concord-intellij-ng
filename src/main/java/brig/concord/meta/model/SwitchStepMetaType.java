package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.psi.YamlPsiUtils;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class SwitchStepMetaType extends IdentityMetaType {

    private static final SwitchStepMetaType INSTANCE = new SwitchStepMetaType();

    public static SwitchStepMetaType getInstance() {
        return INSTANCE;
    }

    protected SwitchStepMetaType() {
        super("Switch", "switch", Set.of("switch"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return Collections.emptyMap();
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        if (name.equals("switch")) {
            return new Field(name, ExpressionMetaType::getInstance);
        }

        return new Field(name, StepsMetaType.getInstance());
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);

        if (value instanceof YAMLMapping m) {
            Set<String> keys = YamlPsiUtils.keys(m);
            if (keys.size() == 1 && keys.contains("switch")) {
                problemsHolder.registerProblem(value, ConcordBundle.message("SwitchStepMetaType.error.missing.labels"), ProblemHighlightType.ERROR);
            }
        }
    }
}
