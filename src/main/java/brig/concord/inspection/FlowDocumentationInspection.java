package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.ConcordType;
import brig.concord.inspection.fix.ReplaceFlowDocKeywordQuickFix;
import brig.concord.inspection.fix.ReplaceFlowDocTypeQuickFix;
import brig.concord.lexer.FlowDocTokenTypes;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FlowDocumentationInspection extends ConcordInspectionTool {


    @Override
    public @NotNull PsiElementVisitor buildConcordVisitor(@NotNull ProblemsHolder holder,
                                                          boolean isOnTheFly) {

        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof FlowDocumentation flowDoc) {
                    checkFlowDocumentation(flowDoc, holder);
                }
            }
        };
    }

    private void checkFlowDocumentation(@NotNull FlowDocumentation flowDoc, @NotNull ProblemsHolder holder) {
        // Check for orphaned documentation (no flow definition follows)
        if (flowDoc.getDocumentedFlow() == null) {
            holder.registerProblem(
                    flowDoc,
                    ConcordBundle.message("inspection.flow.doc.orphaned"),
                    ProblemHighlightType.WARNING
            );
        }

        // Check input parameters
        checkParameters(flowDoc.getInputParameters(), "in", holder);

        // Check output parameters
        checkParameters(flowDoc.getOutputParameters(), "out", holder);
    }

    private void checkParameters(@NotNull List<FlowDocParameter> params,
                                 @NotNull String sectionName,
                                 @NotNull ProblemsHolder holder) {
        // Check for duplicate parameter names
        var seen = new HashMap<String, FlowDocParameter>();
        for (var param : params) {
            var name = param.getName();
            if (name.isEmpty()) {
                continue;
            }

            var prev = seen.putIfAbsent(name, param);
            if (prev != null) {
                holder.registerProblem(
                        param,
                        ConcordBundle.message("inspection.flow.doc.duplicate.param", name, sectionName),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                );
            }

            // Check for unknown types
            var baseType = param.getBaseType();
            if (!baseType.isBlank() && ConcordType.fromString(baseType) == null) {
                var isArray = param.isArrayType();
                var fixes = new ArrayList<LocalQuickFix>();
                for (var suggestion : getSuggestions(baseType)) {
                    var fixType = isArray && !suggestion.endsWith("[]") ? suggestion + "[]" : suggestion;
                    fixes.add(new ReplaceFlowDocTypeQuickFix(fixType));
                }

                holder.registerProblem(
                        param,
                        ConcordBundle.message("inspection.flow.doc.unknown.type", param.getType()),
                        ProblemHighlightType.WARNING,
                        fixes.toArray(LocalQuickFix.EMPTY_ARRAY)
                );
            }

            // Check for unknown keywords (typos in mandatory/optional)
            var unknownKeywordNode = param.getNode().findChildByType(FlowDocTokenTypes.FLOW_DOC_UNKNOWN_KEYWORD);
            if (unknownKeywordNode != null) {
                var fixes = new ArrayList<LocalQuickFix>();
                var text = unknownKeywordNode.getText().toLowerCase();

                if (text.startsWith("opt")) {
                    fixes.add(new ReplaceFlowDocKeywordQuickFix("optional"));
                } else if (text.startsWith("req") || text.startsWith("man")) {
                    fixes.add(new ReplaceFlowDocKeywordQuickFix("mandatory"));
                } else {
                    fixes.add(new ReplaceFlowDocKeywordQuickFix("mandatory"));
                    fixes.add(new ReplaceFlowDocKeywordQuickFix("optional"));
                }

                holder.registerProblem(
                        unknownKeywordNode.getPsi(),
                        ConcordBundle.message("inspection.flow.doc.unknown.keyword", unknownKeywordNode.getText()),
                        ProblemHighlightType.WARNING,
                        fixes.toArray(LocalQuickFix.EMPTY_ARRAY)
                );
            }
        }
    }

    private Collection<String> getSuggestions(String invalidType) {
        var suggestions = new ArrayList<String>();
        // 1. Common abbreviations
        if (invalidType.equalsIgnoreCase("str")) {
            suggestions.add("string");
        }
        if (invalidType.equalsIgnoreCase("bool")) {
            suggestions.add("boolean");
        }
        if (invalidType.equalsIgnoreCase("obj")) {
            suggestions.add("object");
        }
        if (invalidType.equalsIgnoreCase("num")) {
            suggestions.add("number");
        }

        // 2. Case insensitive match against known aliases
        for (var alias : ConcordType.ALIASES.keySet()) {
            if (alias.equalsIgnoreCase(invalidType)) {
                suggestions.add(alias);
            }
        }

        // 3. If "array" or "list" in name, suggest array types
        if (invalidType.toLowerCase().contains("array") || invalidType.toLowerCase().contains("list") || invalidType.endsWith("[]")) {
            suggestions.add("string[]");
            suggestions.add("boolean[]");
            suggestions.add("integer[]");
            suggestions.add("object[]");
            suggestions.add("any[]");
        }

        if (!suggestions.isEmpty()) {
            // deduplicate preserving order
            return new LinkedHashSet<>(suggestions);
        }

        // Fallback: Return commonly used types
        return List.of("string", "boolean", "integer", "object", "any");
    }
}
