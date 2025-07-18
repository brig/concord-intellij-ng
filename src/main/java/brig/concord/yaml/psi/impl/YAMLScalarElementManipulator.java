package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLElementGenerator;
import brig.concord.yaml.psi.YAMLFile;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.List;

public class YAMLScalarElementManipulator extends AbstractElementManipulator<YAMLScalarImpl> {

    @Override
    public @NotNull TextRange getRangeInElement(@NotNull YAMLScalarImpl element) {
        final List<TextRange> ranges = element.getContentRanges();
        if (ranges.isEmpty()) {
            return TextRange.EMPTY_RANGE;
        }

        return TextRange.create(ranges.get(0).getStartOffset(), ranges.get(ranges.size() - 1).getEndOffset());
    }

    @Override
    public YAMLScalarImpl handleContentChange(@NotNull YAMLScalarImpl element, @NotNull TextRange range, String newContent)
            throws IncorrectOperationException {

        try {
            final List<Pair<TextRange, String>> encodeReplacements = element.getEncodeReplacements(newContent);
            final StringBuilder builder = new StringBuilder();
            final String oldText = element.getText();

            builder.append(oldText.subSequence(0, range.getStartOffset()));
            builder.append(YAMLScalarImpl.processReplacements(newContent, encodeReplacements));
            builder.append(oldText.subSequence(range.getEndOffset(), oldText.length()));

            final YAMLFile dummyYamlFile = YAMLElementGenerator.getInstance(element.getProject()).createDummyYamlWithText(builder.toString());
            final YAMLScalar newScalar = PsiTreeUtil.collectElementsOfType(dummyYamlFile, YAMLScalar.class).iterator().next();

            final PsiElement result = element.replace(newScalar);
            if (!(result instanceof YAMLScalarImpl)) {
                throw new AssertionError("Inserted YAML scalar, but it isn't a scalar after insertion :(");
            }

            // it is a hack to preserve the `QUICK_EDIT_HANDLERS` key,
            // actually `element.replace` should have done it, but for some reason didn't
            ((UserDataHolderBase)element.getNode()).copyCopyableDataTo((UserDataHolderBase)result.getNode());
            CodeEditUtil.setNodeGenerated(result.getNode(), true);

            return ((YAMLScalarImpl)result);
        }
        catch (IllegalArgumentException e) {
            final PsiElement newElement = element.replace(YAMLElementGenerator.getInstance(element.getProject()).createYamlDoubleQuotedString());
            if (!(newElement instanceof YAMLQuotedTextImpl)) {
                throw new AssertionError("Could not replace with dummy scalar");
            }
            return handleContentChange((YAMLScalarImpl)newElement, newContent);
        }
    }
}
