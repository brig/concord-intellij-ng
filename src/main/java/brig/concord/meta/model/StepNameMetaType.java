package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.StringMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.Nullable;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class StepNameMetaType extends StringMetaType implements HighlightProvider {

    private static final StepNameMetaType INSTANCE = new StepNameMetaType();

    public static StepNameMetaType getInstance() {
        return INSTANCE;
    }

    private StepNameMetaType() {
        super(descKey("doc.step.feature.name.description"));
    }

    @Override
    public @Nullable TextAttributesKey getValueHighlight(String value) {
        return ConcordHighlightingColors.DSL_LABEL;
    }
}
