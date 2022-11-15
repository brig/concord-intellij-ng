package brig.concord.meta.model;

import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.CompletionContext;
import org.jetbrains.yaml.meta.model.YamlIntegerType;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.List;

public class IntegerMetaType extends YamlIntegerType {

    private static final IntegerMetaType INSTANCE = new IntegerMetaType();

    public static IntegerMetaType getInstance() {
        return INSTANCE;
    }

    public IntegerMetaType() {
        super(false);
    }
}
