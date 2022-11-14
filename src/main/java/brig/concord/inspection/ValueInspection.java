package brig.concord.inspection;

import brig.concord.meta.ConcordMetaTypeProvider;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeProvider;
import org.jetbrains.yaml.meta.impl.YamlUnknownValuesInspectionBase;
import org.jetbrains.yaml.meta.model.*;
import org.jetbrains.yaml.psi.YAMLValue;

public class ValueInspection extends YamlUnknownValuesInspectionBase {

    @Override
    protected @Nullable YamlMetaTypeProvider getMetaTypeProvider(@NotNull ProblemsHolder holder) {
        return ConcordMetaTypeProvider.getInstance(holder.getProject());
    }

    @Override
    protected @NotNull PsiElementVisitor doBuildVisitor(@NotNull ProblemsHolder holder, @NotNull YamlMetaTypeProvider metaTypeProvider) {
        return new ValuesChecker(holder, metaTypeProvider) {
            @Override
            protected void validateMultiplicity(YamlMetaTypeProvider.@NotNull MetaTypeProxy meta, @NotNull YAMLValue value) {
                if (meta.getMetaType() instanceof YamlAnyOfType) {
                    boolean hasArray = false;
                    boolean hasScalar = false;
                    for (YamlMetaType subType : ((YamlAnyOfType) meta.getMetaType()).getSubTypes()) {
                        if (subType instanceof YamlArrayType) {
                            hasArray = true;
                        } else if (subType instanceof YamlScalarType) {
                            hasScalar = true;
                        }
                    }

                    if (hasArray && hasScalar) {
                        return;
                    }
                } else if (meta.getMetaType() instanceof YamlAnything) {
                    return;
                }

                super.validateMultiplicity(meta, value);
            }
        };
    }
}
