// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.structureView;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ConcordStructureViewTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testStructureViewFull() {
        configureFromResource("/structureView/structure-view.concord.yaml");

        EdtTestUtil.runInEdtAndWait(() -> {
            myFixture.testStructureView(svc -> {
                var root = svc.getTreeModel().getRoot();
                Assertions.assertNotNull(root);

//                printStructure(root, 0);

                var children = root.getChildren();
                Assertions.assertNotNull(children);

                var configuration = assertElement(children, "configuration");
                Assertions.assertEquals(1, configuration.getChildren().length);

                var flows = assertElement(children, "flows");
                Assertions.assertEquals(2, flows.getChildren().length);

                var forms =  assertElement(children, "forms");
                Assertions.assertEquals(1, forms.getChildren().length);

                var profiles =  assertElement(children, "profiles");
                Assertions.assertEquals(1, profiles.getChildren().length);

                var resources =  assertElement(children, "resources");
                Assertions.assertEquals(1, resources.getChildren().length);

                var imports =  assertElement(children, "imports");
                Assertions.assertEquals(2, imports.getChildren().length);

                var triggers =  assertElement(children, "triggers");
                Assertions.assertEquals(1, triggers.getChildren().length);

                var publicFlows =  assertElement(children, "publicFlows");
                Assertions.assertEquals(2, publicFlows.getChildren().length);
            });
        });
    }

    @Test
    void testStructureViewEmpty() {
        configureFromResource("/structureView/structure-view-empty.concord.yaml");

        EdtTestUtil.runInEdtAndWait(() -> {
            myFixture.testStructureView(svc -> {
                var root = svc.getTreeModel().getRoot();
                Assertions.assertNotNull(root);

//                printStructure(root, 0);

                var children = root.getChildren();
                Assertions.assertNotNull(children);
                Assertions.assertEquals(0, children.length);
            });
        });
    }

    private static TreeElement assertElement(TreeElement [] elements, String name) {
        var result = Arrays.stream(elements)
                .filter(e -> name.equals(e.getPresentation().getPresentableText()))
                .toList();

        if (result.isEmpty()) {
            throw new AssertionError("Element " + name + " not found");
        } else if (result.size() > 1) {
            throw new AssertionError("Multiple elements found");
        }
        return result.getFirst();
    }

    private static void printStructure(StructureViewTreeElement element, int indent) {
        var prefix = " ".repeat(indent * 2);
        var text = element.getPresentation().getPresentableText();

        System.out.println(prefix + (text != null ? text : "<null>"));

        for (var child : element.getChildren()) {
            printStructure((StructureViewTreeElement) child, indent + 1);
        }
    }
}
