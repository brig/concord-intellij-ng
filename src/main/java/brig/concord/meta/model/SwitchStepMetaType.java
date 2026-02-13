package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class SwitchStepMetaType extends IdentityMetaType {

    private static final SwitchStepMetaType INSTANCE = new SwitchStepMetaType();

    public static SwitchStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "switch", new ExpressionMetaType(desc("doc.type.expression.description").andRequired())
    );

    private SwitchStepMetaType() {
        super("switch", desc("doc.step.switch.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
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
