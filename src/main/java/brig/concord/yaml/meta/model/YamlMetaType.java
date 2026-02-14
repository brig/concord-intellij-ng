package brig.concord.yaml.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.*;
import brig.concord.documentation.Documented;
import brig.concord.yaml.formatter.YAMLCodeStyleSettings;
import brig.concord.yaml.psi.*;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static brig.concord.ConcordBundle.BUNDLE;

public abstract class YamlMetaType implements Documented {
    private final @NotNull String myTypeName;
    private final @Nullable String description;
    private final boolean required;

    protected YamlMetaType(@NonNls @NotNull String typeName) {
        this(typeName, null, null, false);
    }

    protected YamlMetaType(@NonNls @NotNull String typeName, @NotNull TypeProps props) {
        this(typeName, props.descriptionKey(), props.description(), props.isRequired());
    }

    private YamlMetaType(@NotNull String typeName,
                         @Nullable @PropertyKey(resourceBundle = BUNDLE) String descriptionKey,
                         String description,
                         boolean required) {
        this.myTypeName = typeName;
        this.description = descriptionKey != null ? ConcordBundle.message(descriptionKey) : description;
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Contract(pure = true)
    public final @NotNull String getTypeName() {
        return myTypeName;
    }

    public @NotNull String getTypeName(@NotNull String suffix) {
        return myTypeName + suffix;
    }

    @Contract(pure = true)
    public @NotNull String getDisplayName() {
        return myTypeName;
    }

    @Contract(pure = true)
    public @NotNull Icon getIcon() {
        return AllIcons.Json.Object;
    }

    public abstract @Nullable Field findFeatureByName(@NotNull String name);

    /**
     * Computes the set of {@link Field#getName()}s which are missing in the given set of the existing keys.
     *
     */
    public abstract @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields);

    /**
     * Computes the list of fields that should be included into the completion list for the key completion inside the given mapping,
     * which is guaranteed to be typed by <code>this<code/> type.
     * <p/>
     * It is assumed that the list does not depend on the insertion position inside the <code>existingMapping</code>.
     * As an optimisation, the result list may include fields which are already present in the <code>existingMapping</code>, the additional
     * filtering will be done by the caller.
     *
     */
    public abstract @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping);

    public void validateKey(@NotNull YAMLKeyValue keyValue, @NotNull ProblemsHolder problemsHolder) {
        //
    }

    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        //
    }

    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        return Collections.emptyList();
    }

    /**
     * Builds the insertion markup after the feature name, that is, the part starting from ":".
     * <p>
     * E.g for an integer feature with default value, the insertion suffix markup may look like ": 42&lt;crlf&gt;></>", representing the
     * fill insertion markup of "theAnswer: 42&lt;crlf&gt;"
     */
    public abstract void buildInsertionSuffixMarkup(@NotNull YamlInsertionMarkup markup,
                                                    @NotNull Field.Relation relation);

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + myTypeName + "@" + Integer.toHexString(hashCode());
    }

    public static class YamlInsertionMarkup {
        public static final String CRLF_MARKUP = "<crlf>";
        public static final String CARET_MARKUP = "<caret>";
        public static final String SEQUENCE_ITEM_MARKUP = "- ";

        private final StringBuilder myOutput = new StringBuilder();
        private final String myTabSymbol;
        private int myLevel;
        private boolean myCaretAppended;
        private final YAMLCodeStyleSettings mySettings;

        public YamlInsertionMarkup(@NotNull InsertionContext context) {
            this(getTabSymbol(context), CodeStyle.getCustomSettings(context.getFile(), YAMLCodeStyleSettings.class));
        }

        public YamlInsertionMarkup(@NotNull String tabSymbol, YAMLCodeStyleSettings settings) {
            myTabSymbol = tabSymbol;
            mySettings = settings;
        }

        public void append(@NotNull String text) {
            myOutput.append(text);
        }

        public void newLineAndTabs() {
            newLineAndTabs(false);
        }

        public void newLineAndTabs(boolean withSequenceItemMark) {
            assert !withSequenceItemMark || myLevel > 0;

            append(CRLF_MARKUP);
            if (withSequenceItemMark) {
                append(tabs(myLevel - 1));
                append(sequenceItemPrefix());
            }
            else {
                append(tabs(myLevel));
            }
        }

        private @NotNull String sequenceItemPrefix() {
            String result = SEQUENCE_ITEM_MARKUP;
            if (myTabSymbol.length() > result.length()) {
                result += myTabSymbol.substring(result.length());
            }
            return result;
        }

        public void doTabbedBlockForSequenceItem(Runnable doWhenTabbed) {
            var indent = mySettings.INDENT_SEQUENCE_VALUE ? 2 : 1;

            doTabbedBlock(indent, () -> {
                newLineAndTabs(true);
                doWhenTabbed.run();
            });
        }

        public void doTabbedBlockForSequenceItem() {
            doTabbedBlockForSequenceItem(() -> {
            });
        }

        public void appendCaret() {
            if (!myCaretAppended) {
                append(CARET_MARKUP);
            }
            myCaretAppended = true;
        }

        public String getMarkup() {
            return myOutput.toString();
        }

        public void increaseTabs(final int indent) {
            assert indent > 0;
            myLevel += indent;
        }

        public void doTabbedBlock(final int indent, final @NotNull Runnable doWhenTabbed) {
            increaseTabs(indent);
            try {
                doWhenTabbed.run();
            }
            finally {
                decreaseTabs(indent);
            }
        }

        public @NotNull String getTabSymbol() {
            return myTabSymbol;
        }

        public void decreaseTabs(int indent) {
            assert indent <= myLevel;
            myLevel -= indent;
        }

        private String tabs(int level) {
            return StringUtil.repeat(myTabSymbol, level);
        }

        private static @NotNull String getTabSymbol(@NotNull InsertionContext context) {
            return StringUtil.repeatSymbol(' ', CodeStyle.getIndentSize(context.getFile()));
        }

        public void insertStringAndCaret(Editor editor, String commonPadding) {
            String insertionMarkup = getMarkup();
            String suffixWithCaret = insertionMarkup.replace(CRLF_MARKUP, "\n" + commonPadding);
            int caretIndex = suffixWithCaret.indexOf(CARET_MARKUP);
            String suffix = suffixWithCaret.replace(CARET_MARKUP, "");

            EditorModificationUtil.insertStringAtCaret(editor, suffix, false, true, caretIndex);
        }
    }

}
