package brig.concord.hierarchy;

import brig.concord.ConcordBundle;
import brig.concord.ConcordIcons;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.ui.JBColor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Node descriptor for flow hierarchy tree.
 * Displays flow name with file location.
 */
public class FlowHierarchyNodeDescriptor extends HierarchyNodeDescriptor {

    public static final String UNKNOWN_FLOW = "<unknown>";

    private final boolean isDynamic;
    private final String flowName;
    private final boolean isBaseElement;

    public FlowHierarchyNodeDescriptor(
            @NotNull Project project,
            @Nullable NodeDescriptor<?> parentDescriptor,
            @NotNull PsiElement element,
            boolean isBase) {
        super(project, parentDescriptor, element, isBase);
        this.isDynamic = false;
        this.flowName = extractFlowName(element);
        this.isBaseElement = isBase;
    }

    public FlowHierarchyNodeDescriptor(
            @NotNull Project project,
            @Nullable NodeDescriptor<?> parentDescriptor,
            @NotNull PsiElement element,
            boolean isBase,
            boolean isDynamic,
            @NotNull String flowName) {
        super(project, parentDescriptor, element, isBase);
        this.isDynamic = isDynamic;
        this.flowName = flowName;
        this.isBaseElement = isBase;
    }

    @Override
    public boolean update() {
        boolean changes = super.update();
        var oldText = myHighlightedText;

        myHighlightedText = new CompositeAppearance();

        var element = getPsiElement();
        if (element == null || !element.isValid()) {
            return showInvalidElement();
        }

        // Flow name
        var displayName = flowName;
        if (isDynamic) {
            displayName = displayName + " " + ConcordBundle.message("hierarchy.dynamic.marker");
        }

        var nameAttributes = new TextAttributes();
        if (isBaseElement) {
            nameAttributes.setFontType(Font.BOLD);
        }

        myHighlightedText.getEnding().addText(displayName, nameAttributes);

        // File location
        var file = element.getContainingFile();
        if (file != null) {
            var locationText = " (" + getLocationText(element, file) + ")";
            var locationAttributes = new TextAttributes();
            locationAttributes.setForegroundColor(JBColor.GRAY);
            myHighlightedText.getEnding().addText(locationText, locationAttributes);
        }

        myName = myHighlightedText.getText();

        if (!myHighlightedText.equals(oldText)) {
            changes = true;
        }

        return changes;
    }

    @Override
    protected @Nullable Icon getIcon(@NotNull PsiElement element) {
        return ConcordIcons.FLOW;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public @NotNull String getFlowName() {
        return flowName;
    }

    private boolean showInvalidElement() {
        var invalidText = ConcordBundle.message("hierarchy.invalid.element");
        myHighlightedText.getEnding().addText(invalidText, HierarchyNodeDescriptor.getInvalidPrefixAttributes());
        return true;
    }

    private @NotNull String getLocationText(@NotNull PsiElement element, @NotNull PsiFile file) {
        var fileName = file.getName();
        var doc = com.intellij.psi.PsiDocumentManager.getInstance(myProject).getDocument(file);
        if (doc != null) {
            var lineNumber = doc.getLineNumber(element.getTextOffset()) + 1;
            return fileName + ":" + lineNumber;
        }
        return fileName;
    }

    private static @NotNull String extractFlowName(@NotNull PsiElement element) {
        if (element instanceof YAMLKeyValue kv) {
            return kv.getKeyText();
        }
        // For call sites, try to find the containing flow
        var containingFlow = FlowCallFinder.findContainingFlow(element);
        if (containingFlow != null) {
            return containingFlow.getKeyText();
        }
        return UNKNOWN_FLOW;
    }
}
