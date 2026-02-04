package brig.concord.psi;

import com.intellij.util.messages.Topic;

public interface ConcordProjectListener {

    @Topic.ProjectLevel
    Topic<ConcordProjectListener> TOPIC = Topic.create("Concord Project Changes", ConcordProjectListener.class);

    /**
     * Called when Concord project have changed: concord yamls, scopes, dependencies
     */
    void projectChanged();
}
