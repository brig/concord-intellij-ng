package brig.concord.highlighting;

import brig.concord.inspection.fix.InsertClosingMarkerFix;
import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.HighlightProvider;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLQuotedText;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static brig.concord.yaml.YAMLUtil.isNumberValue;

public class ConcordHighlightingAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof YAMLKeyValue keyValue) {
            annotateKey(keyValue, holder);
        } else if (element instanceof YAMLScalar scalar) {
            annotateValue(scalar, holder);
        } else if (element instanceof PsiErrorElement errorElement) {
            annotateError(errorElement, holder);
        }
    }

    private static void annotateError(@NotNull PsiErrorElement errorElement, @NotNull AnnotationHolder holder) {
        var errorDescription = errorElement.getErrorDescription();
        if (!"Expected closing ## marker".equals(errorDescription)) {
            return;
        }

        // Verify parent is FLOW_DOCUMENTATION element
        var parent = errorElement.getParent();
        if (parent == null || parent.getNode().getElementType() != FlowDocElementTypes.FLOW_DOCUMENTATION) {
            return;
        }

        // Add quick fix without creating a new error (parser already shows the error)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(errorElement)
                .withFix(new InsertClosingMarkerFix(errorElement))
                .create();
    }

    @Nullable
    private static HighlightProvider highlightProvider(PsiElement e) {
        var metaTypeProvider = ConcordMetaTypeProvider.getInstance(e.getProject());
        var meta = metaTypeProvider.getMetaTypeProxy(e);
        if (meta == null) {
            return null;
        }

        if (!(meta.getMetaType() instanceof HighlightProvider highlightProvider)) {
            return null;
        }

        return highlightProvider;
    }

    private static void annotateKey(@NotNull YAMLKeyValue kv, @NotNull AnnotationHolder holder) {
        var keyElement = kv.getKey();
        if (keyElement == null) {
            return;
        }

        var keyText = kv.getKeyText();
        if (keyText.isEmpty()) {
            return;
        }

        var highlightProvider = highlightProvider(kv);
        if (highlightProvider == null) {
            return;
        }

        var keyHighlight = highlightProvider.getKeyHighlight(keyText);
        if (keyHighlight != null) {
            highlight(holder, keyElement.getTextRange(), keyHighlight);
        }
    }

    private static void annotateValue(@NotNull YAMLScalar scalar, @NotNull AnnotationHolder holder) {
        var text = scalar.getTextValue();
        if (text.isBlank()) {
            return;
        }

        var highlightedByMeta = highlightByMeta(text, scalar, holder);

        if (YamlPsiUtils.isDynamicExpression(scalar)) {
            return;
        }

        if (highlightedByMeta) {
            return;
        }

        // Quoted scalars are always strings: no boolean/number/null fallback
        if (scalar instanceof YAMLQuotedText) {
            return;
        }

        if (YAMLUtil.isBooleanValue(text)) {
            highlight(holder, scalar.getTextRange(), ConcordHighlightingColors.BOOLEAN);
        } else if (isNullValue(text)) {
            highlight(holder, scalar.getTextRange(), ConcordHighlightingColors.NULL);
        } else if (isNumberValue(text)) {
            highlight(holder, scalar.getTextRange(), ConcordHighlightingColors.NUMBER);
        }
    }

    private static boolean highlightByMeta(String textValue, YAMLScalar value, AnnotationHolder holder) {
        var highlightProvider = highlightProvider(value);
        if (highlightProvider == null) {
            return false;
        }

        var valueHighlight = highlightProvider.getValueHighlight(textValue);
        if (valueHighlight == null) {
            return false;
        }

        highlight(holder, value.getTextRange(), valueHighlight);

        return true;
    }

    private static void highlight(@NotNull AnnotationHolder holder,
                                  @NotNull TextRange range,
                                  @NotNull TextAttributesKey key) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(range)
                .textAttributes(key)
                .create();
    }

    private static boolean isNullValue(String v) {
        return "~".equals(v) || "null".equalsIgnoreCase(v);
    }
}
