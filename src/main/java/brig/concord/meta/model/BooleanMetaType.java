package brig.concord.meta.model;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlBooleanType;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

@SuppressWarnings("UnstableApiUsage")
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
