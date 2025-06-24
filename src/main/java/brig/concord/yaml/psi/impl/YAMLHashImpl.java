package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLElementGenerator;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.psi.YAMLFile;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;

public class YAMLHashImpl extends YAMLMappingImpl implements YAMLMapping {
    public YAMLHashImpl(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected void addNewKey(@NotNull YAMLKeyValue key) {
        PsiElement anchor = null;
        for (PsiElement child = getLastChild(); child != null; child = child.getPrevSibling()) {
            final IElementType type = child.getNode().getElementType();
            if (type == YAMLTokenTypes.COMMA || type == YAMLTokenTypes.LBRACE) {
                anchor = child;
            }
        }

        addAfter(key, anchor);

        final YAMLFile dummyFile = YAMLElementGenerator.getInstance(getProject()).createDummyYamlWithText("{,}");
        final PsiElement comma = dummyFile.findElementAt(1);
        assert comma != null && comma.getNode().getElementType() == YAMLTokenTypes.COMMA;

        addAfter(comma, key);
    }

    @Override
    public String toString() {
        return "YAML hash";
    }
}
