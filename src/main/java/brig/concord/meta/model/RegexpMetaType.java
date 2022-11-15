package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlStringType;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexpMetaType extends StringMetaType {

    private static final RegexpMetaType INSTANCE = new RegexpMetaType();

    public static RegexpMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        try {
            Pattern.compile(scalarValue.getTextValue());
        } catch (PatternSyntaxException e) {
            holder.registerProblem(scalarValue, ConcordBundle.message("RegexpType.error.scalar.value"), ProblemHighlightType.ERROR);
        }
    }
}
