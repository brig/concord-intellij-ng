// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import org.jetbrains.annotations.NotNull;

public interface CompletionContext {
    @NotNull
    CompletionType getCompletionType();

    int getInvocationCount();

    @NotNull
    String getCompletionPrefix();

    @NotNull
    CompletionResultSet getCompletionResultSet();
}
