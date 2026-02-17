// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi;

import com.intellij.util.messages.Topic;

public interface ConcordDependenciesListener {

    Topic<ConcordDependenciesListener> TOPIC =
            Topic.create("Concord Dependencies Changes", ConcordDependenciesListener.class);

    void dependenciesChanged();
}
