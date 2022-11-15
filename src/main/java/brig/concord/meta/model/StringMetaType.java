package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLBundle;
import org.jetbrains.yaml.meta.model.YamlStringType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

public class StringMetaType extends YamlStringType {

    private static final StringMetaType INSTANCE = new StringMetaType();

    public static StringMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar value, @NotNull ProblemsHolder holder) {
        String text = value.getTextValue();
        if (text.matches("[0-9]+")) {
            holder.registerProblem(value, ConcordBundle.message("StringType.error.scalar.value"), ProblemHighlightType.ERROR);
        }
    }
}
