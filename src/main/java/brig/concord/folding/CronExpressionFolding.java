package brig.concord.folding;

import brig.concord.psi.ProcessDefinitionProvider;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.openapi.util.TextRange;
import brig.concord.psi.ConcordFile;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class CronExpressionFolding extends CustomFoldingBuilder {

    private static final Pattern INVALID_CHARS_REGEX = Pattern.compile("[^\\d|A-Z? \\-,/*#]");
    private static final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    private static final CronDescriptor descriptor = CronDescriptor.instance(Locale.getDefault());

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root, @NotNull Document document, boolean quick) {
        if (!(root.getContainingFile() instanceof ConcordFile)) {
            return;
        }

        ProcessDefinitionProvider.getInstance().get(root).triggers().stream()
                .flatMap(seqItem -> seqItem.getKeysValues().stream())
                .filter(kv -> kv.getKey() != null && "cron".equals(kv.getKey().getText()))
                .map(YAMLKeyValue::getValue)
                .filter(v -> v instanceof YAMLMapping)
                .map(YAMLMapping.class::cast)
                .map(mapping -> mapping.getKeyValueByKey("spec"))
                .filter(Objects::nonNull)
                .map(YAMLKeyValue::getValue)
                .filter(Objects::nonNull)
                .forEach(v -> descriptors.add(new FoldingDescriptor(v, v.getTextRange())));
    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        var text = node.getText().trim().replaceAll("'", "").replaceAll("\"", "");
        if (!isCronExpression(text)) {
            return null;
        }

        try {
            return descriptor.describe(parser.parse(text));
        } catch (Exception e) {
            // ignore
        }

        return "invalid cron expression";
    }

    private static boolean isCronExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }

        int expressionParts = expression.split(" ", 8).length;

        if (expressionParts < 5 || expressionParts > 7) {
            return false;
        }

        return !INVALID_CHARS_REGEX.matcher(expression).find();
    }
}
