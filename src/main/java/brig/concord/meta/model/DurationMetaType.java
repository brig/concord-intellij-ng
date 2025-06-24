package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLScalar;

import java.time.Duration;
import java.time.format.DateTimeParseException;

public class DurationMetaType extends StringMetaType {

    private static final DurationMetaType INSTANCE = new DurationMetaType();

    public static DurationMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        super.validateScalarValue(scalarValue, holder);

        try {
            Duration.parse(scalarValue.getTextValue());
        } catch (DateTimeParseException e) {
            holder.registerProblem(scalarValue, ConcordBundle.message("DurationType.error.scalar.value"), ProblemHighlightType.ERROR);
        }
    }
}
