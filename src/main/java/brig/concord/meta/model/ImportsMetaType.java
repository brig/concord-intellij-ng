package brig.concord.meta.model;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlArrayType;
import org.jetbrains.yaml.psi.YAMLValue;

public class ImportsMetaType extends YamlArrayType {

    private static final ImportsMetaType INSTANCE = new ImportsMetaType();

    public static ImportsMetaType getInstance() {
        return INSTANCE;
    }

    public ImportsMetaType() {
        super(ImportElementMetaType.getInstance());
    }
}
