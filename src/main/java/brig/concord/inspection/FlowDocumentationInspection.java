package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.lexer.FlowDocTokenTypes;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FlowDocumentationInspection extends LocalInspectionTool {

    private static final Set<String> VALID_TYPES = Set.of(
            "string", "boolean", "int", "integer", "number", "object", "any",
            "string[]", "boolean[]", "int[]", "integer[]", "number[]", "object[]", "any[]"
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                                   boolean isOnTheFly) {
        if (!(holder.getFile() instanceof ConcordFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

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
            var type = param.getType();
            if (!VALID_TYPES.contains(type)) {
                holder.registerProblem(
                        param,
                        ConcordBundle.message("inspection.flow.doc.unknown.type", type),
                        ProblemHighlightType.WARNING
                );
            }

            // Check for unknown keywords (typos in mandatory/optional)
            var unknownKeywordNode = param.getNode().findChildByType(FlowDocTokenTypes.FLOW_DOC_UNKNOWN_KEYWORD);
            if (unknownKeywordNode != null) {
                holder.registerProblem(
                        unknownKeywordNode.getPsi(),
                        ConcordBundle.message("inspection.flow.doc.unknown.keyword", unknownKeywordNode.getText()),
                        ProblemHighlightType.WARNING
                );
            }
        }
    }
}
