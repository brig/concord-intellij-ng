// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import com.intellij.psi.PsiFile;

import java.util.List;

public interface YAMLFile extends PsiFile, YAMLPsiElement {
    List<YAMLDocument> getDocuments();
}
