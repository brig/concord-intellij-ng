package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.CompletionContext;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class StepElementMetaType extends IdentityElementMetaType {

    private static final List<IdentityMetaType> steps = List.of(
            TaskStepMetaType.getInstance(),
            CallStepMetaType.getInstance(),
            LogStepMetaType.getInstance(),
            IfStepMetaType.getInstance(),
            ExitStepMetaType.getInstance(),
            CheckpointStepMetaType.getInstance(),
            SetStepMetaType.getInstance(),
            ThrowStepMetaType.getInstance(),
            SuspendStepMetaType.getInstance(),
            ExprStepMetaType.getInstance(),
            ParallelStepMetaType.getInstance(),
            ScriptStepMetaType.getInstance(),
            SwitchStepMetaType.getInstance(),
            TryStepMetaType.getInstance(),
            BlockStepMetaType.getInstance(),
            FormStepMetaType.getInstance()
    );

    private static final StepElementMetaType INSTANCE = new StepElementMetaType();

    public static StepElementMetaType getInstance() {
        return INSTANCE;
    }

    protected StepElementMetaType() {
        super("Steps", List.copyOf(steps));
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLScalar) {
            if ("return".equals(value.getText())) {
                return;
            }

            problemsHolder.registerProblem(value, ConcordBundle.message("StepElementMetaType.error.unknown.step"), ProblemHighlightType.ERROR);
        }

        super.validateValue(value, problemsHolder);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        if (insertedScalar.getParent() instanceof YAMLSequenceItem) {
            LookupElementBuilder l = LookupElementBuilder
                    .create("return");
            List result = super.getValueLookups(insertedScalar, completionContext);
            result.add(l);
            return result;
        }
        return Collections.emptyList();
    }
}
