package brig.concord.inlay.call;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.FlowDocParameter;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.codeInsight.hints.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CallInlayHintsProvider implements InlayHintsProvider<NoSettings> {

    private static final SettingsKey<NoSettings> KEY = new SettingsKey<>("concord.call.inlay");

    @NotNull
    @Override
    public InlayHintsCollector getCollectorFor(
            @NotNull PsiFile file,
            @NotNull Editor editor,
            @NotNull NoSettings settings,
            @NotNull InlayHintsSink sink
    ) {
        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(
                    @NotNull PsiElement element,
                    @NotNull Editor editor,
                    @NotNull InlayHintsSink sink
            ) {
                if (!(element instanceof YAMLKeyValue keyValue)) {
                    return true;
                }

                var hintText = getHintText(keyValue);
                if (hintText == null) {
                    return true;
                }

                var keyElement = keyValue.getKey();
                if (keyElement == null) {
                    return true;
                }

                var presentation = getFactory().smallText(hintText);
                sink.addInlineElement(keyElement.getTextRange().getEndOffset(), true, presentation, false);
                return true;
            }
        };
    }

    @Override
    public @NotNull SettingsKey<NoSettings> getKey() {
        return KEY;
    }

    @Override
    public @NotNull String getName() {
        return "Call parameters";
    }

    @Override
    public @NotNull InlayGroup getGroup() {
        return InlayGroup.PARAMETERS_GROUP;
    }

    @Override
    public @NotNull NoSettings createSettings() {
        return new NoSettings();
    }

    @Override
    public @Nullable String getPreviewText() {
        return null;
    }

    @Override
    public @NotNull ImmediateConfigurable createConfigurable(@NotNull NoSettings settings) {
        return new ImmediateConfigurable() {
            @Override
            public @NotNull JComponent createComponent(@NotNull ChangeListener listener) {
                return new JPanel();
            }
        };
    }

    @Nullable
    private static String getHintText(@NotNull YAMLKeyValue keyValue) {
        if (!isInCallInParams(keyValue)) {
            return null;
        }

        var definition = FlowCallParamsProvider.getInstance().inParamDefinition(keyValue);
        if (!(definition instanceof FlowDocParameter param)) {
            return null;
        }

        var type = param.getType();
        if (type.isBlank()) {
            return null;
        }

        if (param.isMandatory()) {
            return " (" + type + ", mandatory)";
        }
        return " (" + type + ")";
    }

    private static boolean isInCallInParams(@NotNull YAMLKeyValue keyValue) {
        var parentMapping = keyValue.getParentMapping();
        if (parentMapping == null) {
            return false;
        }

        var inKeyValue = PsiTreeUtil.getParentOfType(parentMapping, YAMLKeyValue.class);
        if (inKeyValue == null || !"in".equals(inKeyValue.getKeyText())) {
            return false;
        }

        return FlowCallParamsProvider.findCallKv(keyValue) != null;
    }
}
