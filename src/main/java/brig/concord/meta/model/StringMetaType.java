package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlStringType;
import brig.concord.yaml.psi.YAMLCompoundValue;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;

public class StringMetaType extends YamlStringType {

    private static final StringMetaType INSTANCE = new StringMetaType();

    public static StringMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar value, @NotNull ProblemsHolder holder) {
        String text = value.getText();
        if (text.startsWith("\"") && text.endsWith("\"")) {
            return;
        }

        if (text.matches("[0-9]+")) {
            holder.registerProblem(value, ConcordBundle.message("StringType.error.scalar.value"), ProblemHighlightType.ERROR);
        }
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLScalar) {
            validateScalarValue((YAMLScalar)value, problemsHolder);
        }
        else if (value instanceof YAMLCompoundValue) {
            problemsHolder.registerProblem(value, ConcordBundle.message("StringType.error.scalar.value"), ProblemHighlightType.ERROR);
        }
    }
}
