package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.psi.YAMLQuotedText;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;

public class BooleanMetaType extends YamlEnumType {

    private static final BooleanMetaType INSTANCE = new BooleanMetaType();

    public static BooleanMetaType getInstance() {
        return INSTANCE;
    }

    public BooleanMetaType() {
        super("yaml:boolean", "boolean");
        withLiterals("true", "false", "TRUE", "FALSE", "True",  "False");
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        if (scalarValue instanceof YAMLQuotedText) {
            //TODO: quickfix would be nice here
            holder.registerProblem(scalarValue,
                    ConcordBundle.message("YamlBooleanType.validation.error.quoted.value"),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            return;
        }

        super.validateScalarValue(scalarValue, holder);
    }
}
