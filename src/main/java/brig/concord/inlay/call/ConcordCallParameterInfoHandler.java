package brig.concord.inlay.call;

import brig.concord.psi.FlowDocumentation;
import brig.concord.psi.ref.FlowDefinitionReference;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConcordCallParameterInfoHandler
        implements ParameterInfoHandler<YAMLScalar, ConcordFlowSignature> {

    @Override
    public @Nullable YAMLScalar findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        var element = findCallValue(context.getFile().findElementAt(context.getOffset()));
        if (element == null) {
            return null;
        }

        var signature = buildSignature(element);
        if (signature == null) {
            return null;
        }

        context.setItemsToShow(new Object[]{signature});
        return element;
    }

    @Override
    public @Nullable YAMLScalar findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        return findCallValue(context.getFile().findElementAt(context.getOffset()));
    }

    @Override
    public void showParameterInfo(@NotNull YAMLScalar element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, element.getTextRange().getStartOffset(), this);
    }

    @Override
    public void updateParameterInfo(@NotNull YAMLScalar place, @NotNull UpdateParameterInfoContext context) {
        // если хочешь подсвечивать "текущий параметр" — можно обновлять currentParameter
    }

    @Override
    public void updateUI(ConcordFlowSignature sig, @NotNull ParameterInfoUIContext context) {
        // тут рисуешь текст попапа (in/out, mandatory/optional)
        String text = sig.renderForPopup();
        context.setupUIComponentPresentation(
                text,
                0, text.length(),   // диапазон подсветки (можно 0,0 если пока не надо)
                false, false, false,
                context.getDefaultParameterColor()
        );
    }

    private static @Nullable YAMLScalar findCallValue(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }

        var scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar.class, true);
        if (scalar == null) {
            return null;
        }

        var keyValue = PsiTreeUtil.getParentOfType(scalar, YAMLKeyValue.class);
        if (keyValue == null) {
            return null;
        }

        if (!"call".equals(keyValue.getKeyText())) {
            return null;
        }

        if (keyValue.getValue() != scalar) {
            return null;
        }

        return scalar;
    }

    private static @Nullable ConcordFlowSignature buildSignature(@NotNull YAMLScalar callValue) {
        var doc = findFlowDocumentation(callValue);
        if (doc == null) {
            return null;
        }
        return ConcordFlowSignature.from(doc);
    }

    private static @Nullable FlowDocumentation findFlowDocumentation(@NotNull YAMLScalar callValue) {
        for (var ref : callValue.getReferences()) {
            if (ref instanceof FlowDefinitionReference fdr) {
                var definition = fdr.resolve();
                if (definition != null) {
                    return findFlowDocumentationBefore(definition);
                }
            }
        }
        return null;
    }

    private static @Nullable FlowDocumentation findFlowDocumentationBefore(@NotNull PsiElement flowDefinition) {
        var sibling = flowDefinition.getPrevSibling();
        while (sibling != null) {
            if (sibling instanceof FlowDocumentation doc) {
                return doc;
            }
            if (sibling.getTextLength() > 0 && !sibling.getText().isBlank()) {
                break;
            }
            sibling = sibling.getPrevSibling();
        }
        return null;
    }
}
