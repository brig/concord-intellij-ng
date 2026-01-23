package brig.concord.inspection;

import brig.concord.inspection.fix.AddParameterToFlowDocQuickFix;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.AnyOfType;
import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.model.call.CallInParamsMetaType;
import brig.concord.psi.ConcordFile;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.impl.YamlMetaTypeInspectionBase;
import brig.concord.yaml.meta.impl.YamlMetaTypeProvider;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlScalarType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLValue;

public class UnknownKeysInspection extends YamlMetaTypeInspectionBase {

    @Override
    @NotNull
    protected PsiElementVisitor doBuildVisitor(@NotNull ProblemsHolder holder, @NotNull YamlMetaTypeProvider metaTypeProvider) {
        return new StructureChecker(holder, metaTypeProvider);
    }

    private static class StructureChecker extends SimpleYamlPsiVisitor {
        private final YamlMetaTypeProvider myMetaTypeProvider;
        private final ProblemsHolder myProblemsHolder;

        StructureChecker(@NotNull ProblemsHolder problemsHolder, @NotNull YamlMetaTypeProvider metaTypeProvider) {
            myProblemsHolder = problemsHolder;
            myMetaTypeProvider = metaTypeProvider;
        }

        @Override
        protected void visitYAMLKeyValue(@NotNull YAMLKeyValue keyValue) {
            if (keyValue.getKey() == null) {
                return;
            }

            if ("<<".equals(keyValue.getKey().getText())) {
                // validation of merge types is not supported, but at least there should be no red code
                return;
            }

            YamlMetaTypeProvider.MetaTypeProxy meta = myMetaTypeProvider.getKeyValueMetaType(keyValue);
            if (meta != null) {
                return;
            }

            YAMLValue parent = keyValue.getParentMapping();
            if (parent != null) {
                YamlMetaTypeProvider.MetaTypeProxy typeProxy = myMetaTypeProvider.getValueMetaType(parent);

                if (typeProxy == null) {
                    return;
                }

                // Only mark the first element as unknown, not its children
                //
                // Also if it's a mapping instead of expected scalar type,
                // don't report key-specific errors as they're redundant ("scalar value expected" error is already reported)
                YamlMetaType parentMetaType = typeProxy.getMetaType();
                if (parentMetaType instanceof CallInParamsMetaType) {
                    String msg = ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", keyValue.getKeyText());
                    var flowDoc = FlowCallParamsProvider.findFlowDocumentation(keyValue);
                    if (flowDoc != null) {
                        myProblemsHolder.registerProblem(keyValue.getKey(), msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                                new AddParameterToFlowDocQuickFix(keyValue.getKey(), keyValue.getKeyText(), flowDoc));
                    } else {
                        myProblemsHolder.registerProblem(keyValue.getKey(), msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                    }
                    return;
                }

                if(parentMetaType instanceof YamlScalarType || parentMetaType instanceof YamlArrayType) {
                    return;
                }

                if (parentMetaType instanceof AnyOfType any) {
                    if (any.isScalar()) {
                        return;
                    }
                }
            }

            String msg = ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", keyValue.getKeyText());
            myProblemsHolder.registerProblem(keyValue.getKey(), msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
        }
    }

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
}
