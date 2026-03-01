// SPDX-License-Identifier: Apache-2.0
package brig.concord.completion;

import brig.concord.dependency.TaskInfo;
import brig.concord.dependency.TaskMethod;
import brig.concord.dependency.TaskRegistry;
import brig.concord.el.psi.*;
import brig.concord.psi.BuiltInFunction;
import brig.concord.psi.BuiltInFunctions;
import brig.concord.psi.ElAccessChainResolver;
import brig.concord.psi.VariableSource;
import brig.concord.psi.VariablesProvider;
import brig.concord.schema.SchemaProperty;
import brig.concord.schema.SchemaType;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ElCompletionContributor extends CompletionContributor {

    public record VariableLookup(@NotNull String name, @NotNull VariableSource source,
                                 @NotNull SchemaProperty schema) {
    }

    public record PropertyLookup(@NotNull String name, @NotNull SchemaProperty schema) {
    }

    public record FunctionLookup(@NotNull BuiltInFunction function) {
    }

    public record TaskNameElLookup(@NotNull String name) {
    }

    public record TaskMethodLookup(@NotNull String taskName, @NotNull TaskMethod method) {
    }

    public ElCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElVariableCompletionProvider());

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElFunctionCompletionProvider());

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElTaskNameCompletionProvider());

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElPropertyCompletionProvider());

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElTaskMethodCompletionProvider());
    }

    private static class ElVariableCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();

            // Skip completion after dot (property access) or in function names
            var parent = position.getParent();
            if (parent instanceof ElMemberName || parent instanceof ElFunctionExpr) {
                return;
            }

            // Only offer completions for standalone identifiers
            if (!(parent instanceof ElIdentifierExpr)) {
                return;
            }

            // Navigate from EL PSI up to the enclosing YAML element
            var yamlElement = PsiTreeUtil.getParentOfType(position, YAMLValue.class);
            if (yamlElement == null) {
                return;
            }

            var variables = VariablesProvider.getVariables(yamlElement);
            for (var variable : variables) {
                var schema = variable.schema();
                var lookup = new VariableLookup(variable.name(), variable.source(), schema);
                var builder = LookupElementBuilder.create(lookup, variable.name())
                        .withIcon(AllIcons.Nodes.Variable)
                        .withTailText("  " + variable.source().shortLabel(), true)
                        .withTypeText(SchemaType.displayName(schema.schemaType()));
                result.addElement(builder);
            }
        }
    }

    private static class ElFunctionCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();

            var parent = position.getParent();
            if (parent instanceof ElMemberName || parent instanceof ElFunctionExpr) {
                return;
            }

            if (!(parent instanceof ElIdentifierExpr)) {
                return;
            }

            for (var function : BuiltInFunctions.getInstance().getAll()) {
                var lookup = new FunctionLookup(function);
                var builder = LookupElementBuilder.create(lookup, function.name())
                        .withIcon(AllIcons.Nodes.Method)
                        .withTailText(function.signature(), true)
                        .withTypeText(SchemaType.displayName(function.returnType()))
                        .withInsertHandler((ctx, item) -> {
                            var editor = ctx.getEditor();
                            EditorModificationUtil.insertStringAtCaret(editor, "()");
                            if (!function.params().isEmpty()) {
                                editor.getCaretModel().moveCaretRelatively(-1, 0, false, false, false);
                            }
                        });
                result.addElement(builder);
            }
        }
    }

    private static class ElTaskNameCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();

            var parent = position.getParent();
            if (parent instanceof ElMemberName || parent instanceof ElFunctionExpr) {
                return;
            }

            if (!(parent instanceof ElIdentifierExpr)) {
                return;
            }

            var yamlElement = PsiTreeUtil.getParentOfType(position, YAMLValue.class);
            if (yamlElement == null) {
                return;
            }

            var taskInfos = TaskRegistry.getInstance(yamlElement.getProject()).getTaskInfos(yamlElement);
            for (var taskInfo : taskInfos.values()) {
                var lookup = new TaskNameElLookup(taskInfo.name());
                var builder = LookupElementBuilder.create(lookup, taskInfo.name())
                        .withIcon(AllIcons.Nodes.Class)
                        .withTailText("  task", true);
                result.addElement(builder);
            }
        }
    }

    private static class ElPropertyCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();
            var parent = position.getParent();

            if (!(parent instanceof ElMemberName memberName)) {
                return;
            }

            var objectSchema = ElAccessChainResolver.resolvePropertiesAtCaret(memberName);
            if (objectSchema == null) {
                return;
            }

            for (var entry : objectSchema.properties().entrySet()) {
                var prop = entry.getValue();
                var lookup = new PropertyLookup(entry.getKey(), prop);
                var builder = LookupElementBuilder.create(lookup, entry.getKey())
                        .withIcon(AllIcons.Nodes.Property)
                        .withTypeText(SchemaType.displayName(prop.schemaType()));

                result.addElement(builder);
            }
        }
    }

    private static class ElTaskMethodCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();
            var parent = position.getParent();

            if (!(parent instanceof ElMemberName memberName)) {
                return;
            }

            var dotSuffix = memberName.getParent();
            if (!(dotSuffix instanceof ElDotSuffix)) {
                return;
            }

            var accessExpr = dotSuffix.getParent();
            if (!(accessExpr instanceof ElAccessExpr access)) {
                return;
            }

            // Only complete on the first dot suffix (direct task method call)
            var suffixes = access.getSuffixList();
            if (suffixes.isEmpty() || suffixes.get(0) != dotSuffix) {
                return;
            }

            var baseExpr = access.getExpression();
            if (!(baseExpr instanceof ElIdentifierExpr identExpr)) {
                return;
            }

            var taskName = identExpr.getIdentifier().getText();

            var yamlElement = PsiTreeUtil.getParentOfType(position, YAMLValue.class);
            if (yamlElement == null) {
                return;
            }

            var taskInfo = TaskRegistry.getInstance(yamlElement.getProject()).getTaskInfo(yamlElement, taskName);
            if (taskInfo == null) {
                return;
            }

            for (var method : taskInfo.methods()) {
                var lookup = new TaskMethodLookup(taskName, method);
                var lookupKey = method.name() + method.signature();
                var builder = LookupElementBuilder.create(lookup, lookupKey)
                        .withPresentableText(method.name())
                        .withIcon(AllIcons.Nodes.Method)
                        .withTailText(method.signature(), true)
                        .withTypeText(SchemaType.displayName(method.returnType()))
                        .withInsertHandler((ctx, item) -> {
                            var document = ctx.getDocument();
                            document.replaceString(ctx.getStartOffset(), ctx.getTailOffset(), method.name());
                            ctx.setTailOffset(ctx.getStartOffset() + method.name().length());
                            var editor = ctx.getEditor();
                            editor.getCaretModel().moveToOffset(ctx.getTailOffset());
                            EditorModificationUtil.insertStringAtCaret(editor, "()");
                            if (!method.parameterTypes().isEmpty()) {
                                editor.getCaretModel().moveCaretRelatively(-1, 0, false, false, false);
                            }
                        });
                result.addElement(builder);
            }
        }
    }
}
