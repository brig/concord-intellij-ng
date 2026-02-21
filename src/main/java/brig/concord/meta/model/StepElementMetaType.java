// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallStepMetaType;
import brig.concord.yaml.meta.model.CompletionContext;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLSequenceItem;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StepElementMetaType extends IdentityElementMetaType implements HighlightProvider {

    private static final List<IdentityMetaType> steps = List.of(
            TaskStepMetaType.getInstance(),
            CallStepMetaType.getInstance(),
            LogStepMetaType.getInstance(),
            LogYamlStepMetaType.getInstance(),
            IfStepMetaType.getInstance(),
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

    private StepElementMetaType() {
        super(List.copyOf(steps));
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLScalar scalar) {
            if ("return".equals(scalar.getTextValue())) {
                return;
            }
            if ("exit".equals(scalar.getTextValue())) {
                return;
            }

            problemsHolder.registerProblem(value, ConcordBundle.message("StepElementMetaType.error.unknown.step"), ProblemHighlightType.ERROR);
        }

        super.validateValue(value, problemsHolder);
    }

    @Override
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        if (!(insertedScalar.getParent() instanceof YAMLSequenceItem)) {
            return List.of();
        }

        var result = new ArrayList<LookupElement>(
                super.getValueLookups(insertedScalar, completionContext)
        );

        result.add(LookupElementBuilder.create("return"));
        result.add(LookupElementBuilder.create("exit"));

        return result;
    }

    private static final Set<String> CONTROL_KEYWORDS = Set.of("then", "else",  "default");

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        if (CONTROL_KEYWORDS.contains(key)) {
            return ConcordHighlightingColors.STEP_KEYWORD;
        }

        for (var step : steps) {
            if (step.getIdentity().equals(key)) {
                return ConcordHighlightingColors.STEP_KEYWORD;
            }
        }

        return ConcordHighlightingColors.DSL_KEY;
    }

    @Override
    public @Nullable TextAttributesKey getValueHighlight(String value) {
        if ("return".equals(value) || "exit".equals(value)) {
            return ConcordHighlightingColors.STEP_KEYWORD;
        }
        return null;
    }
}
