// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLElementTypes;
import brig.concord.ConcordFileType;
import brig.concord.ConcordLanguage;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLFile;

import java.util.ArrayList;
import java.util.List;

public class YAMLFileImpl extends PsiFileBase implements YAMLFile {

    public YAMLFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, ConcordLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return ConcordFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "YAML file: " + getName();
    }

    @Override
    public List<YAMLDocument> getDocuments() {
        final ArrayList<YAMLDocument> result = new ArrayList<>();
        for (ASTNode node : getNode().getChildren(TokenSet.create(YAMLElementTypes.DOCUMENT))) {
            result.add((YAMLDocument) node.getPsi());
        }
        return result;
    }
}
