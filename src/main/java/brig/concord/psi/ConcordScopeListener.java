package brig.concord.psi;

import com.intellij.util.messages.Topic;

/**
 * Listener for Concord scope changes (roots added/removed/modified).
 */
public interface ConcordScopeListener {

    @Topic.ProjectLevel
    Topic<ConcordScopeListener> TOPIC = Topic.create("Concord Scope Changes", ConcordScopeListener.class);

    /**
     * Called when Concord scopes have changed.
     */
    void scopesChanged();
}
