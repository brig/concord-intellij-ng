package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlScalarType;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.regex.Pattern;

public class ExpressionMetaType extends YamlScalarType {

    private static final ExpressionMetaType INSTANCE = new ExpressionMetaType();

    private static final Pattern EXPR_PATTERN = Pattern.compile("^\\$\\{.*}$", Pattern.DOTALL);

    protected ExpressionMetaType() {
        super("Expression", "expression");
    }

    public static ExpressionMetaType getInstance() {
        return INSTANCE;
    }

    public static boolean containsExpression(String value) {
        return value.contains("${");
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        String text = scalarValue.getTextValue();
        if (!EXPR_PATTERN.matcher(text).matches()) {
            holder.registerProblem(scalarValue, ConcordBundle.message("ExpressionType.error.invalid.value"), ProblemHighlightType.ERROR);
        }
    }
}
