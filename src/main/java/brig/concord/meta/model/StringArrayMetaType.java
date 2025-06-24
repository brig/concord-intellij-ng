package brig.concord.meta.model;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.psi.YAMLValue;

public class StringArrayMetaType extends YamlArrayType {

    private static final StringArrayMetaType INSTANCE = new StringArrayMetaType();

    public static StringArrayMetaType getInstance() {
        return INSTANCE;
    }

    public StringArrayMetaType() {
        super(StringMetaType.getInstance());
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);
    }
}
