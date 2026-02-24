// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package brig.concord.yaml.annotator;

import brig.concord.ConcordBundle;
import brig.concord.psi.FlowDocumentation;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.YAMLAnchor;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLSequenceItem;
import brig.concord.yaml.psi.YAMLValue;
import brig.concord.yaml.psi.impl.YAMLBlockMappingImpl;
import brig.concord.yaml.psi.impl.YAMLBlockSequenceImpl;
import brig.concord.yaml.psi.impl.YAMLKeyValueImpl;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class YAMLInvalidBlockChildrenErrorAnnotator implements Annotator, DumbAware {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof YAMLBlockMappingImpl) && !(element instanceof YAMLBlockSequenceImpl)) {
            return;
        }

        // language injection (script steps) produces OuterLanguageElement children
        // that break YAML structural rules — skip such mappings
        if (element instanceof YAMLBlockMappingImpl mapping && mapping.getKeyValueByKey("script") != null) {
            return;
        }

        if (anotherErrorWillBeReported(element)) {
            return;
        }

        if (reportSameLineWarning(element, holder)) {
            return;
        }

        if (element instanceof YAMLBlockMappingImpl mapping) {
            if (!isValidBlockMapChild(mapping.getFirstChild())) {
                var reportElement = mapping.getFirstKeyValue().getKey();
                if (reportElement == null) {
                    reportElement = mapping.getFirstKeyValue();
                }
                reportWholeElementProblem(holder, mapping, reportElement);
                return;
            }

            var invalid = findFirstInvalidChild(mapping, false);
            if (invalid != null) {
                reportSubElementProblem(holder,
                        ConcordBundle.message("inspections.invalid.child.in.block.mapping"), invalid);
            }

            checkIndent(List.copyOf(mapping.getKeyValues()), holder,
                    ConcordBundle.message("inspections.invalid.key.indent"));
        }

        if (element instanceof YAMLBlockSequenceImpl sequence) {
            if (!isValidBlockSequenceChild(sequence.getFirstChild())) {
                var items = sequence.getItems();
                reportWholeElementProblem(holder, sequence,
                        items.isEmpty() ? sequence : items.getFirst());
                return;
            }

            var invalid = findFirstInvalidChild(sequence, true);
            if (invalid != null) {
                reportSubElementProblem(holder,
                        ConcordBundle.message("inspections.invalid.child.in.block.sequence"), invalid);
            }

            checkIndent(List.copyOf(sequence.getItems()), holder,
                    ConcordBundle.message("inspections.invalid.list.item.indent"));
        }
    }

    private static @Nullable PsiElement findFirstInvalidChild(@NotNull PsiElement parent, boolean sequence) {
        for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (sequence ? !isValidBlockSequenceChild(child) : !isValidBlockMapChild(child)) {
                return child;
            }
        }
        return null;
    }

    private static void reportWholeElementProblem(@NotNull AnnotationHolder holder,
                                                  @NotNull PsiElement element,
                                                  @NotNull PsiElement reportElement) {
        holder.newAnnotation(HighlightSeverity.ERROR, getMessageForParent(element))
                .range(TextRange.create(element.getTextOffset(), endOfLine(reportElement, element)))
                .create();
    }

    private static int endOfLine(@NotNull PsiElement subElement, @NotNull PsiElement whole) {
        var current = subElement;
        while (true) {
            var next = PsiTreeUtil.nextLeaf(current);
            if (next == null) {
                break;
            }
            if (PsiUtilCore.getElementType(next) == YAMLTokenTypes.EOL) {
                break;
            }
            current = next;
            if (current.getTextRange().getEndOffset() >= whole.getTextRange().getEndOffset()) {
                break;
            }
        }
        return Math.min(current.getTextRange().getEndOffset(), whole.getTextRange().getEndOffset());
    }

    private static void checkIndent(@NotNull List<? extends PsiElement> elements,
                                    @NotNull AnnotationHolder holder,
                                    @NotNull String message) {
        if (elements.size() > 1) {
            int firstIndent = YAMLUtil.getIndentToThisElement(elements.getFirst());
            for (var item : elements.subList(1, elements.size())) {
                if (YAMLUtil.getIndentToThisElement(item) != firstIndent) {
                    reportSubElementProblem(holder, message, item);
                }
            }
        }
    }

    private static @NotNull String getMessageForParent(@NotNull PsiElement element) {
        if (findNeededParent(element) instanceof YAMLKeyValueImpl) {
            return ConcordBundle.message("inspections.invalid.child.in.block.mapping");
        }
        return ConcordBundle.message("inspections.invalid.child.in.block.sequence");
    }

    private static boolean isValidBlockMapChild(@Nullable PsiElement element) {
        return element instanceof YAMLKeyValue
                || element instanceof YAMLAnchor
                || element instanceof LeafPsiElement
                || element instanceof FlowDocumentation;
    }

    private static boolean isValidBlockSequenceChild(@Nullable PsiElement element) {
        return element instanceof YAMLSequenceItem
                || element instanceof YAMLAnchor
                || element instanceof LeafPsiElement;
    }

    private static boolean anotherErrorWillBeReported(@NotNull PsiElement element) {
        var kvParent = findNeededParent(element);
        if (kvParent == null) {
            return false;
        }
        var kvGrandParent = PsiTreeUtil.getParentOfType(kvParent, YAMLKeyValueImpl.class, true);
        if (kvGrandParent == null) {
            return false;
        }
        return YAMLUtil.psiAreAtTheSameLine(kvGrandParent, element);
    }

    private static @Nullable PsiElement findNeededParent(@NotNull PsiElement element) {
        return PsiTreeUtil.findFirstParent(element, true,
                e -> e instanceof YAMLKeyValueImpl || e instanceof YAMLSequenceItem);
    }

    private static boolean reportSameLineWarning(@NotNull PsiElement value, @NotNull AnnotationHolder holder) {
        var parent = value.getParent();
        if (!(parent instanceof YAMLKeyValue keyValue)) {
            return false;
        }
        var key = keyValue.getKey();
        if (key == null) {
            return false;
        }
        if (value instanceof YAMLBlockMappingImpl mapping) {
            if (YAMLUtil.psiAreAtTheSameLine(key, mapping.getFirstKeyValue())) {
                reportAboutSameLine(holder, mapping);
                return true;
            }
        }
        if (value instanceof YAMLBlockSequenceImpl sequence) {
            var items = sequence.getItems();
            if (items.isEmpty()) {
                return true;
            }
            if (YAMLUtil.psiAreAtTheSameLine(key, items.getFirst())) {
                reportAboutSameLine(holder, sequence);
                return true;
            }
        }
        return false;
    }

    private static void reportAboutSameLine(@NotNull AnnotationHolder holder, @NotNull YAMLValue value) {
        reportSubElementProblem(holder, ConcordBundle.message("annotator.same.line.composed.value.message"), value);
    }

    private static void reportSubElementProblem(@NotNull AnnotationHolder holder,
                                                @NotNull String message,
                                                @NotNull PsiElement subElement) {
        var firstLeaf = TreeUtil.findFirstLeaf(subElement.getNode());
        if (firstLeaf == null) {
            return;
        }
        holder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(TextRange.create(subElement.getTextOffset(), endOfLine(firstLeaf.getPsi(), subElement)))
                .create();
    }
}