// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.settings;

import brig.concord.ConcordBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Parent configurable that groups all Concord settings under Tools > Concord.
 */
public final class ConcordSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    public static final String ID = "brig.concord.settings";

    @Override
    public @NotNull @NonNls String getId() {
        return ID;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return ConcordBundle.message("settings.display.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
    }
}
