package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlScalarType;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ExpressionMetaType extends YamlScalarType {

    private static final ExpressionMetaType INSTANCE = new ExpressionMetaType();

    private static final Pattern EXPR_PATTERN = Pattern.compile("^\\$\\{.*}$", Pattern.DOTALL);

    public ExpressionMetaType() {
        super("expression", descKey("doc.type.expression.description"));
    }

    public ExpressionMetaType(@NotNull TypeProps props) {
        super("expression", props);
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
