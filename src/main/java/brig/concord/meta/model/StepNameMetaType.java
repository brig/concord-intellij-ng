package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.Nullable;

public class StepNameMetaType extends StringMetaType implements HighlightProvider {

    private static final StepNameMetaType INSTANCE = new StepNameMetaType();

    public static StepNameMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    public @Nullable TextAttributesKey getValueHighlight(String value) {
        return ConcordHighlightingColors.DSL_LABEL;
    }
}
