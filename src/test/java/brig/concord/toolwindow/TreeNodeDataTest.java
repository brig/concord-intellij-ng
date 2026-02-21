// SPDX-License-Identifier: Apache-2.0
package brig.concord.toolwindow;

import brig.concord.toolwindow.ConcordProjectsPanel.TreeNodeData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeNodeDataTest {

    @Test
    void toStringReturnsDisplayName() {
        var data = new TreeNodeData("myScope", null, List.of(), List.of());
        assertEquals("myScope", data.toString());
    }

    @Test
    void toStringIgnoresChildren() {
        var child = new TreeNodeData("child", null, List.of(), List.of());
        var parent = new TreeNodeData("parent", null, List.of(), List.of(child));
        assertEquals("parent", parent.toString());
    }
}
