package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class FlowsMetaType extends MapMetaType {

    private static final FlowsMetaType INSTANCE = new FlowsMetaType();

    public static FlowsMetaType getInstance() {
        return INSTANCE;
    }

    private static final List<Field> defaultCompletions = List.of(new Field("default", StepsMetaType.getInstance()));

    protected FlowsMetaType() {
        super("FLows");
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        return defaultCompletions;
//        String placeholder = "flowName";
//        return List.of(new Field(placeholder, getMapEntryType(null)) {
//            @Override
//            public @NotNull List<LookupElementBuilder> getKeyLookups(@NotNull YamlMetaType ownerClass, @NotNull PsiElement insertedScalar) {
//                LookupElementBuilder lookup = LookupElementBuilder
//                        .create(new TypeFieldPair(ownerClass, this), getName())
//                        .withTypeText(getMapEntryType(null).getDisplayName(), true)
//                        .withIcon(getLookupIcon())
//                        .withStrikeoutness(isDeprecated())
//                        .withInsertHandler((context, item) -> {
//                            Editor editor = context.getEditor();
//                            editor.getCaretModel().moveToOffset(context.getStartOffset());
//                            SelectionModel model = editor.getSelectionModel();
//                            model.setSelection(context.getStartOffset(), context.getStartOffset() + placeholder.length());
//                        });
//
//                if (isRequired()) {
//                    lookup = lookup.bold();
//                }
//                return Collections.singletonList(lookup);
//            }
//        });
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return StepsMetaType.getInstance();
    }
}
