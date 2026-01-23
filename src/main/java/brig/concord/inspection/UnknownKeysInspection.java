package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.inspection.fix.AddParameterToFlowDocQuickFix;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.AnyOfType;
import brig.concord.meta.model.call.CallInParamsMetaType;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.meta.impl.YamlMetaTypeInspectionBase;
import brig.concord.yaml.meta.impl.YamlMetaTypeProvider;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlScalarType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

            if ("<<".equals(keyValue.getKeyText())) {
                return;
            }

            if (myMetaTypeProvider.getKeyValueMetaType(keyValue) != null) {
                return;
            }

            YAMLValue parent = keyValue.getParentMapping();
            if (parent == null) {
                return;
            }

            var typeProxy = myMetaTypeProvider.getValueMetaType(parent);
            if (typeProxy == null) {
                return;
            }

            var parentMetaType = typeProxy.getMetaType();
            if (parentMetaType instanceof CallInParamsMetaType) {
                handleCallInParams(keyValue);
                return;
            }

            if (shouldIgnore(parentMetaType)) {
                return;
            }

            registerUnknownKeyProblem(keyValue);
        }

        private void handleCallInParams(@NotNull YAMLKeyValue keyValue) {
            var flowDoc = FlowCallParamsProvider.findFlowDocumentation(keyValue);
            if (flowDoc != null && keyValue.getKey() != null) {
                var quickFix = new AddParameterToFlowDocQuickFix(keyValue.getKey(), keyValue.getKeyText(), flowDoc);
                registerUnknownKeyProblem(keyValue, quickFix);
            } else {
                registerUnknownKeyProblem(keyValue);
            }
        }

        private void registerUnknownKeyProblem(@NotNull YAMLKeyValue keyValue, @NotNull LocalQuickFix... fixes) {
            var msg = ConcordBundle.message("YamlUnknownKeysInspectionBase.unknown.key", keyValue.getKeyText());
            var keyElement = keyValue.getKey();
            if (keyElement != null) {
                myProblemsHolder.registerProblem(keyElement, msg, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, fixes);
            }
        }

        private static boolean shouldIgnore(YamlMetaType type) {
            if (type instanceof YamlScalarType || type instanceof YamlArrayType) {
                return true;
            }
            if (type instanceof AnyOfType any) {
                return any.isScalar();
            }
            return false;
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
