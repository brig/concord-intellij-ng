// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.Nullable;

public interface HighlightProvider {

    default @Nullable TextAttributesKey getKeyHighlight(String key) {
        return null;
    }

    default @Nullable TextAttributesKey getValueHighlight(String value) {
        return null;
    }
}
