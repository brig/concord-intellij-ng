// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi;

import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConcordIconProvider extends IconProvider {

    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        if (!(element instanceof YAMLKeyValue kv)) {
            return null;
        }

        if (!(element.getContainingFile() instanceof ConcordFile)) {
            return null;
        }

        var parent = kv.getParent();
        if (!(parent instanceof YAMLMapping mapping)) {
            return null;
        }

        var grandParent = mapping.getParent();
        if (grandParent instanceof YAMLDocument) {
            return getSectionIcon(kv.getKeyText());
        }

        if (grandParent instanceof YAMLKeyValue parentKv) {
            var sectionName = parentKv.getKeyText();
            return getElementIcon(sectionName);
        }

        return null;
    }

    private @Nullable Icon getSectionIcon(String key) {
        return switch (key) {
            case "flows" -> ConcordIcons.FLOWS;
            case "forms" -> ConcordIcons.FORMS;
            case "triggers" -> ConcordIcons.TRIGGERS;
            case "publicFlows" -> ConcordIcons.PUBLIC_FLOWS;
            case "profiles" -> ConcordIcons.PROFILES;
            case "resources" -> ConcordIcons.RESOURCES;
            case "imports" -> ConcordIcons.IMPORTS;
            case "configuration" -> ConcordIcons.CONFIGURATION;
            default -> null;
        };
    }

    private @Nullable Icon getElementIcon(String sectionName) {
        return switch (sectionName) {
            case "flows" -> ConcordIcons.FLOW;
            case "forms" -> ConcordIcons.FORM;
            case "triggers" -> ConcordIcons.TRIGGER;
            case "publicFlows" -> ConcordIcons.PUBLIC_FLOW;
            case "profiles" -> ConcordIcons.PROFILE;
            case "imports" -> ConcordIcons.IMPORT;
            default -> null;
        };
    }
}
