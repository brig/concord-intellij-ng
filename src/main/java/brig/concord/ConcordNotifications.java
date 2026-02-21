// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import org.jetbrains.annotations.NotNull;

public final class ConcordNotifications {
    
    public static final String GROUP_ID = "Concord Notification Group";

    private ConcordNotifications() {
    }

    @NotNull
    public static NotificationGroup getGroup() {
        return NotificationGroupManager.getInstance().getNotificationGroup(GROUP_ID);
    }
}
