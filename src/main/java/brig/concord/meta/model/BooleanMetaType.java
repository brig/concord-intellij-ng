package brig.concord.meta.model;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlBooleanType;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;

public class BooleanMetaType extends YamlBooleanType {

    private static final BooleanMetaType INSTANCE = new BooleanMetaType();

    public static BooleanMetaType getInstance() {
        return INSTANCE;
    }

    public BooleanMetaType() {
        super("boolean");
        withLiterals("true", "false");

        withHiddenLiterals(new LiteralBuilder()
                .withAllCasesOf("true")
                .withAllCasesOf("false")
                .withAllCasesOf("on", "off", "yes", "no")
                .toArray());
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        super.validateScalarValue(scalarValue, holder);
    }
}
