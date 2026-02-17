// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.YAMLElementGenerator;
import brig.concord.yaml.YAMLElementTypes;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.YAMLKeyValue;

import java.util.Collection;
import java.util.List;

public class YAMLBlockMappingImpl extends YAMLMappingImpl {
    public static final String EMPTY_MAP_MESSAGE = "YAML map without any key-value";

    public YAMLBlockMappingImpl(@NotNull ASTNode node) {
        super(node);
    }

    public @NotNull YAMLKeyValue getFirstKeyValue() {
        YAMLKeyValue firstKeyValue = findChildByType(YAMLElementTypes.KEY_VALUE_PAIR);
        if (firstKeyValue == null) {
            throw new IllegalStateException(EMPTY_MAP_MESSAGE);
        }
        return firstKeyValue;
    }

    @Override
    protected void addNewKey(@NotNull YAMLKeyValue key) {
        final int indent = YAMLUtil.getIndentToThisElement(this);
        ASTNode node = getNode();
        ASTNode place = node.getLastChildNode();
        ASTNode whereInsert = null;
        while(place != null) {
            if(place.getElementType() == YAMLTokenTypes.INDENT && place.getTextLength() == indent) {
                whereInsert = place;
            }
            else if (place.getElementType() == YAMLTokenTypes.EOL) {
                ASTNode next = place.getTreeNext();
                if (next == null || next.getElementType() == YAMLTokenTypes.EOL) {
                    whereInsert = place;
                }
            }
            else {
                break;
            }
            place = place.getTreePrev();
        }

        final YAMLElementGenerator generator = YAMLElementGenerator.getInstance(getProject());
        if (whereInsert == null) {
            add(generator.createEol());
            if (indent != 0) {
                add(generator.createIndent(indent));
            }
            add(key);
            return;
        }

        PsiElement anchor = whereInsert.getPsi();
        if (indent == 0 || whereInsert.getElementType() == YAMLTokenTypes.INDENT && getLastChild().getTextLength() == indent) {
            addAfter(key, anchor);
            return;
        }
        if (whereInsert.getElementType() != YAMLTokenTypes.EOL) {
            anchor = addAfter(generator.createEol(), anchor);
        }
        addAfter(generator.createIndent(indent), anchor);
        addAfter(key, anchor);
    }
}
