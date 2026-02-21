// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;

public final class ConcordProblemFileHighlightFilter implements Condition<VirtualFile> {

    @Override
    public boolean value(VirtualFile file) {
        return file.getFileType() == ConcordFileType.INSTANCE;
    }
}
