package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YamlPsiElementVisitor;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuplicatedKeysInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                                   boolean isOnTheFly) {
        if (!(holder.getFile() instanceof ConcordFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return new YamlPsiElementVisitor() {
            @Override
            public void visitMapping(@NotNull YAMLMapping mapping) {
                checkForDuplicateKeys(mapping, holder);
            }
        };
    }

    private static void checkForDuplicateKeys(@NotNull YAMLMapping mapping, @NotNull ProblemsHolder holder) {
        // key -> first occurrence (the pair)
        var first = new HashMap<String, YAMLKeyValue>();
        // key -> all duplicates after first
        var dups = new HashMap<String, List<YAMLKeyValue>>();

        for (var kv : mapping.getKeyValues()) {
            var keyText = keyText(kv);
            if (keyText == null) {
                continue;
            }

            var prev = first.putIfAbsent(keyText, kv);
            if (prev != null) {
                dups.computeIfAbsent(keyText, k -> new ArrayList<>()).add(kv);
            }
        }

        if (dups.isEmpty()) return;

        for (var e : dups.entrySet()) {
            var key = e.getKey();
            for (var dupKv : e.getValue()) {
                var keyElement = dupKv.getKey();
                if (keyElement == null) continue;

                holder.registerProblem(
                        keyElement,
                        ConcordBundle.message("inspection.duplicate.keys.message", key),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                );
            }
        }
    }

    private static String keyText(@NotNull YAMLKeyValue kv) {
        var kt = kv.getKeyText();
        return kt.isBlank() ? null : kt;
    }
}
