// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.formatter;


import brig.concord.yaml.YAMLTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.AbstractWhiteSpaceFormattingStrategy;
import com.intellij.util.SmartList;
import com.intellij.psi.tree.IElementType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class YamlWhiteSpaceFormattingStrategy extends AbstractWhiteSpaceFormattingStrategy {

    @Override
    public int check(CharSequence text, int start, int end) {
        return start;
    }

    @Override
    public CharSequence adjustWhiteSpaceIfNecessary(CharSequence whiteSpaceText,
                                                    CharSequence text,
                                                    int startOffset,
                                                    int endOffset,
                                                    CodeStyleSettings codeStyleSettings,
                                                    ASTNode nodeAfter) {
        IElementType elementType = nodeAfter != null ? nodeAfter.getElementType() : null;
        if (YAMLTokenTypes.SEQUENCE_MARKER.equals(elementType)) {
            return whiteSpaceText;
        }

        List<Integer> lineBreaksPositions = new SmartList<>();
        for (int i = 0; i < whiteSpaceText.length(); i++) {
            if (whiteSpaceText.charAt(i) == '\n') {
                lineBreaksPositions.add(i);
            }
        }
        lineBreaksPositions.add(whiteSpaceText.length());

        List<CharSequence> split = new ArrayList<>();
        for (int i = 0; i < lineBreaksPositions.size() - 1; i++) {
            int from = lineBreaksPositions.get(i);
            int to = lineBreaksPositions.get(i + 1);
            split.add(whiteSpaceText.subSequence(from, to));
        }

        if (split.size() <= 1 || split.stream().noneMatch(s -> s.length() == 1)) {
            return whiteSpaceText;
        }

        CharSequence withIndent = split.stream()
                .filter(s -> s.length() > 1)
                .min(Comparator.comparingInt(CharSequence::length))
                .orElse(whiteSpaceText);

        StringBuilder result = new StringBuilder();
        for (CharSequence part : split) {
            if (part.length() == 1) {
                result.append(withIndent);
            } else {
                result.append(part);
            }
        }

        return result.toString();
    }
}
