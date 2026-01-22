package brig.concord.psi.impl;

import brig.concord.ConcordBundle;
import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.psi.FlowDocParameter;
import brig.concord.yaml.YAMLElementGenerator;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static brig.concord.lexer.FlowDocTokenTypes.*;

public class FlowDocParameterImpl extends ASTWrapperPsiElement implements FlowDocParameter {

    public FlowDocParameterImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        var nameNode = getNode().findChildByType(FLOW_DOC_PARAM_NAME);
        return nameNode != null ? nameNode.getText() : "";
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        if (name.equals(getName())) {
            throw new IncorrectOperationException(ConcordBundle.message("rename.same.name"));
        }

        var nameNode = getNode().findChildByType(FLOW_DOC_PARAM_NAME);
        if (nameNode == null) {
            throw new IncorrectOperationException("Cannot find parameter name node");
        }

        var generator = YAMLElementGenerator.getInstance(getProject());
        var dummyParam = generator.createFlowDocParameter(name, "string");
        var dummyNameNode = dummyParam.getNode().findChildByType(FLOW_DOC_PARAM_NAME);
        if (dummyNameNode == null) {
            throw new IncorrectOperationException("Failed to get dummy parameter name node");
        }

        // Replace the old name node with the new one
        nameNode.getPsi().replace(dummyNameNode.getPsi());
        return this;
    }

    @Override
    public PsiElement setType(@NotNull String type) throws IncorrectOperationException {
        var node = getNode();
        var typeNode = node.findChildByType(FLOW_DOC_TYPE);
        if (typeNode == null) {
            typeNode = node.findChildByType(FLOW_DOC_ARRAY_TYPE);
        }

        if (typeNode == null) {
            throw new IncorrectOperationException("Cannot find parameter type node");
        }

        var generator = YAMLElementGenerator.getInstance(getProject());
        var dummyParam = generator.createFlowDocParameter("dummy", type);

        var dummyTypeNode = dummyParam.getNode().findChildByType(FLOW_DOC_TYPE);
        if (dummyTypeNode == null) {
            dummyTypeNode = dummyParam.getNode().findChildByType(FLOW_DOC_ARRAY_TYPE);
        }

        if (dummyTypeNode == null) {
            throw new IncorrectOperationException("Failed to create dummy parameter type node");
        }

        node.replaceChild(typeNode, dummyTypeNode);
        return this;
    }

    @Override
    @NotNull
    public String getType() {
        var typeNode = getNode().findChildByType(FLOW_DOC_TYPE);
        if (typeNode != null) {
            return typeNode.getText();
        }

        var arrayTypeNode = getNode().findChildByType(FLOW_DOC_ARRAY_TYPE);
        if (arrayTypeNode != null) {
            return arrayTypeNode.getText();
        }

        return "any";
    }

    @Override
    public boolean isArrayType() {
        return getType().endsWith("[]");
    }

    @Override
    @NotNull
    public String getBaseType() {
        var type = getType();
        if (type.endsWith("[]")) {
            return type.substring(0, type.length() - 2);
        }
        return type;
    }

    @Override
    public boolean isMandatory() {
        // FLOW_DOC_MANDATORY token matches both "mandatory" and "required"
        return getNode().findChildByType(FLOW_DOC_MANDATORY) != null;
    }

    @Override
    @Nullable
    public String getDescription() {
        var descNode = getNode().findChildByType(FLOW_DOC_TEXT);
        if (descNode != null) {
            var text = descNode.getText().trim();
            // Remove leading comma if present
            if (text.startsWith(",")) {
                text = text.substring(1).trim();
            }
            return text.isEmpty() ? null : text;
        }
        return null;
    }

    @Override
    public boolean isInputParameter() {
        var parent = getParent();
        if (parent != null) {
            return parent.getNode().getElementType() == FlowDocElementTypes.FLOW_DOC_IN_SECTION;
        }
        return false;
    }

    @Override
    public boolean isOutputParameter() {
        var parent = getParent();
        if (parent != null) {
            return parent.getNode().getElementType() == FlowDocElementTypes.FLOW_DOC_OUT_SECTION;
        }
        return false;
    }

    @Override
    @Nullable
    public PsiReference getReference() {
        var refs = getReferences();
        return refs.length > 0 ? refs[0] : null;
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public String toString() {
        return "FlowDocParameter(" + getName() + ": " + getType() + ")";
    }
}
