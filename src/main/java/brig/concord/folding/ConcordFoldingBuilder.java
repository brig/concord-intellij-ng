// SPDX-License-Identifier: Apache-2.0
package brig.concord.folding;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.*;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ConcordFoldingBuilder extends CustomFoldingBuilder {

    private static final int PLACEHOLDER_LEN = 20;

    private static final Map<String, String> IMPORT_PRIMARY_KEYS = Map.of(
            "git", "url",
            "dir", "src",
            "mvn", "url"
    );

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors,
                                            @NotNull PsiElement root,
                                            @NotNull Document document,
                                            boolean quick) {
        if (!(root.getContainingFile() instanceof ConcordFile concordFile)) {
            return;
        }

        var provider = ConcordMetaTypeProvider.getInstance(root.getProject());
        concordFile.getDocument().ifPresent(doc -> {
            if (doc.getTopLevelValue() instanceof YAMLMapping mapping) {
                collectFoldsForMapping(mapping, provider, descriptors);
            }
        });
    }

    private static void collectFoldsForMapping(@NotNull YAMLMapping mapping,
                                               @NotNull ConcordMetaTypeProvider provider,
                                               @NotNull List<FoldingDescriptor> descriptors) {
        for (var kv : mapping.getKeyValues()) {
            var childValue = kv.getValue();
            if (!(childValue instanceof YAMLCompoundValue)) {
                continue;
            }

            var childMeta = provider.getBaseMetaType(childValue);
            addDescriptor(descriptors, kv, keyValuePlaceholder(kv));

            if (childValue instanceof YAMLSequence seq) {
                var recurse = childMeta instanceof StepElementMetaType;
                collectFoldsForSequence(seq, childMeta, provider, descriptors, recurse);
            } else if (shouldRecurseMapping(childMeta)) {
                collectFoldsForValue(childValue, provider, descriptors);
            }
        }
    }

    private static void collectFoldsForSequence(@NotNull YAMLSequence sequence,
                                                @Nullable YamlMetaType elementMeta,
                                                @NotNull ConcordMetaTypeProvider provider,
                                                @NotNull List<FoldingDescriptor> descriptors,
                                                boolean recurseIntoItems) {
        for (var item : sequence.getItems()) {
            var itemValue = item.getValue();
            if (!(itemValue instanceof YAMLCompoundValue)) {
                continue;
            }

            addDescriptor(descriptors, item, sequenceItemPlaceholder(item, elementMeta));

            if (recurseIntoItems && itemValue instanceof YAMLMapping itemMapping) {
                collectFoldsForMapping(itemMapping, provider, descriptors);
            }
        }
    }

    private static @NotNull String sequenceItemPlaceholder(@NotNull YAMLSequenceItem item, @Nullable YamlMetaType elementMeta) {
        if (!(item.getValue() instanceof YAMLMapping mapping)) {
            return "- ...";
        }

        if (elementMeta instanceof StepElementMetaType step) {
            return stepPlaceholder(mapping, step);
        }
        if (elementMeta instanceof TriggerElementMetaType trigger) {
            return triggerPlaceholder(mapping, trigger);
        }
        if (elementMeta instanceof ImportElementMetaType imp) {
            return importPlaceholder(mapping, imp);
        }
        return "- ...";
    }

    private static @NotNull String getValuedPlaceholder(@NotNull YAMLMapping mapping, @NotNull String key) {
        var kv = mapping.getKeyValueByKey(key);
        if (kv == null) {
            return "- " + key + ": ...";
        }

        var val = kv.getValue();
        if (val instanceof YAMLScalar scalar) {
            return "- " + key + ": " + normalizePlaceholderText(scalar.getTextValue());
        }
        return "- " + key + ": ...";
    }

    private static @NotNull String stepPlaceholder(@NotNull YAMLMapping mapping, @NotNull StepElementMetaType meta) {
        var identity = meta.findEntry(mapping);
        return identity != null ? getValuedPlaceholder(mapping, identity.getIdentity()) : "- ...";
    }

    private static @NotNull String triggerPlaceholder(@NotNull YAMLMapping mapping, @NotNull TriggerElementMetaType meta) {
        var identity = meta.findEntry(mapping);
        return identity != null ? "- " + identity.getIdentity() + ": ..." : "- ...";
    }

    private static @NotNull String importPlaceholder(@NotNull YAMLMapping mapping, @NotNull ImportElementMetaType meta) {
        var identity = meta.findEntry(mapping);
        if (identity == null) {
            return "- ...";
        }

        var identityKey = identity.getIdentity();
        var identityKv = mapping.getKeyValueByKey(identityKey);

        if (identityKv != null && identityKv.getValue() instanceof YAMLMapping entryMapping) {
            var primaryKey = IMPORT_PRIMARY_KEYS.get(identityKey);
            if (primaryKey != null) {
                var primaryKv = entryMapping.getKeyValueByKey(primaryKey);
                if (primaryKv != null && primaryKv.getValue() instanceof YAMLScalar scalar) {
                    return "- " + identityKey + ": " + normalizePlaceholderText(scalar.getTextValue());
                }
            }
        }
        return getValuedPlaceholder(mapping, identityKey);
    }

    private static void collectFoldsForValue(@NotNull YAMLValue value,
                                             @NotNull ConcordMetaTypeProvider provider,
                                             @NotNull List<FoldingDescriptor> descriptors) {
        if (value instanceof YAMLMapping mapping) {
            collectFoldsForMapping(mapping, provider, descriptors);
        }
    }

    private static boolean shouldRecurseMapping(@Nullable YamlMetaType metaType) {
        if (metaType == null) {
            return false;
        }
        if (metaType instanceof AnyMapMetaType) {
            return false;
        }
        return metaType instanceof ConcordMetaType;
    }

    private static void addDescriptor(@NotNull List<FoldingDescriptor> descriptors,
                                      @NotNull PsiElement element,
                                      @NotNull String placeholder) {
        var descriptor = new FoldingDescriptor(element, element.getTextRange());
        descriptor.setPlaceholderText(placeholder);
        descriptors.add(descriptor);
    }

    @Override
    protected @Nullable String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return "...";
    }

    private static @NotNull String keyValuePlaceholder(@NotNull YAMLKeyValue kv) {
        return normalizePlaceholderText(kv.getKeyText()) + ": ...";
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    private static @NotNull String normalizePlaceholderText(@NotNull String text) {
        if (text.length() <= PLACEHOLDER_LEN) {
            return text;
        }
        return StringUtil.trimMiddle(text, PLACEHOLDER_LEN);
    }
}
