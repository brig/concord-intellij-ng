// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

public class ConcordLanguage extends Language {

    public static final ConcordLanguage INSTANCE = new ConcordLanguage();

    protected ConcordLanguage() {
        super("Concord");
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Concord";
    }
}
