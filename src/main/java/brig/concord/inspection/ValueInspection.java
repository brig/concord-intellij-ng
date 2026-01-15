package brig.concord.inspection;

import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.MetaUtils;
import brig.concord.psi.ConcordFile;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.impl.YamlMetaTypeProvider;
import brig.concord.yaml.meta.impl.YamlMetaUtil;
import brig.concord.yaml.meta.impl.YamlUnknownValuesInspectionBase;
import brig.concord.yaml.meta.model.*;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLSequence;
import brig.concord.yaml.psi.YAMLValue;

public class ValueInspection extends YamlUnknownValuesInspectionBase {

    @Override
    protected @Nullable YamlMetaTypeProvider getMetaTypeProvider(@NotNull ProblemsHolder holder) {
        return ConcordMetaTypeProvider.getInstance(holder.getProject());
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        if (!(holder.getFile() instanceof ConcordFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return super.buildVisitor(holder, isOnTheFly, session);
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
                } else if (MetaUtils.isAnything(meta.getMetaType())) {
                    return;
                }

                super.validateMultiplicity(meta, value);
            }

            @Override
            protected void visitYAMLKeyValue(@NotNull YAMLKeyValue keyValue) {
                if (keyValue.getKey() == null) {
                    return;
                }

                YamlMetaTypeProvider.MetaTypeProxy meta = metaTypeProvider.getKeyValueMetaType(keyValue);
                if (meta == null) {
                    return;
                }

                YAMLValue value = keyValue.getValue();
                if (value == null || YamlMetaUtil.isNull(value)) {
                    validateEmptyValue(meta.getField(), keyValue);
                    return;
                }

                if (meta instanceof YamlMetaTypeProvider.FieldAndRelation far) {
                    if (far.getRelation() == Field.Relation.SEQUENCE_ITEM && !(value instanceof YAMLSequence)) {
                        holder.registerProblem(value, ConcordBundle.message("YamlUnknownValuesInspectionBase.error.array.is.required"));
                    }
                }

                int before = holder.getResultCount();
                validateMultiplicity(meta, value);
                int after = holder.getResultCount();
                if (before != after) {
                    return;
                }

                meta.getMetaType().validateKey(keyValue, holder);

                if (value instanceof YAMLMapping || value instanceof YAMLSequence) {
                    // will be handled separately
                    return;
                }

                meta.getMetaType().validateValue(value, holder);
            }
        };
    }
}
