package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexpMetaType extends StringMetaType {

    private static final RegexpMetaType INSTANCE = new RegexpMetaType();

    public static RegexpMetaType getInstance() {
        return INSTANCE;
    }

    public RegexpMetaType() {
        super("regexp");
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        super.validateScalarValue(scalarValue, holder);

        try {
            Pattern.compile(scalarValue.getTextValue());
        } catch (PatternSyntaxException e) {
            holder.registerProblem(scalarValue, ConcordBundle.message("RegexpType.error.scalar.value", e.getMessage()), ProblemHighlightType.ERROR);
        }
    }
}
