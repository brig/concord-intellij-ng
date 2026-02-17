// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.formatter;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.NonNls;

public class YAMLCommenter implements Commenter {
    private static final @NonNls String LINE_COMMENT_PREFIX = "#";

    @Override
    public String getLineCommentPrefix() {
        return LINE_COMMENT_PREFIX;
    }

    @Override
    public String getBlockCommentPrefix() {
        // N/A
        return null;
    }

    @Override
    public String getBlockCommentSuffix() {
        // N/A
        return null;
    }

    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}

