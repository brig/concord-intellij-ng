// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml;

import brig.concord.ConcordFileType;
import com.intellij.psi.tree.IElementType;

public class YAMLElementType extends IElementType {

    public YAMLElementType(String debugName) {
        super(debugName, ConcordFileType.INSTANCE.getLanguage());
    }
}
