// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
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
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static brig.concord.lexer.FlowDocTokenTypes.*;

public class FlowDocParameterImpl extends ASTWrapperPsiElement implements FlowDocParameter {

    private static final IElementType[] TYPE_TOKENS = {FLOW_DOC_TYPE, FLOW_DOC_ARRAY_TYPE};
    private static final IElementType[] KEYWORD_TOKENS = {FLOW_DOC_UNKNOWN_KEYWORD, FLOW_DOC_MANDATORY, FLOW_DOC_OPTIONAL};

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

        var generator = YAMLElementGenerator.getInstance(getProject());
        var dummyParam = generator.createFlowDocParameter(name, "string");

        replaceNode(FLOW_DOC_PARAM_NAME, dummyParam, FLOW_DOC_PARAM_NAME, "Cannot find parameter name node");
        return this;
    }

    @Override
    public PsiElement setType(@NotNull String type) throws IncorrectOperationException {
        var generator = YAMLElementGenerator.getInstance(getProject());
        var dummyParam = generator.createFlowDocParameter("dummy", type);

        replaceNode(TYPE_TOKENS, dummyParam, TYPE_TOKENS, "Cannot find parameter type node");
        return this;
    }

    @Override
    public PsiElement setKeyword(@NotNull String keyword) throws IncorrectOperationException {
        var generator = YAMLElementGenerator.getInstance(getProject());
        // Create a dummy parameter with the new keyword.
        var dummyParam = generator.createFlowDocParameter("dummy", "string, " + keyword);

        replaceNode(KEYWORD_TOKENS, dummyParam, KEYWORD_TOKENS, "Cannot find parameter keyword node");
        return this;
    }

    @Override
    @NotNull
    public String getType() {
        var typeNode = findChildByTypes(TYPE_TOKENS);
        return typeNode != null ? typeNode.getText() : "any";
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

    private void replaceNode(IElementType typeToReplace, FlowDocParameter dummyParam, IElementType dummyTypeToFind, String errorMessage) {
        replaceNode(new IElementType[]{typeToReplace}, dummyParam, new IElementType[]{dummyTypeToFind}, errorMessage);
    }

    private void replaceNode(IElementType[] typesToReplace, FlowDocParameter dummyParam, IElementType[] dummyTypesToFind, String errorMessage) {
        var node = getNode();
        var nodeToReplace = findChildByTypes(node, typesToReplace);
        if (nodeToReplace == null) {
            throw new IncorrectOperationException(errorMessage);
        }

        var dummyNode = findChildByTypes(dummyParam.getNode(), dummyTypesToFind);
        if (dummyNode == null) {
            throw new IncorrectOperationException("Failed to create replacement node: " + errorMessage);
        }

        node.replaceChild(nodeToReplace, dummyNode);
    }

    @Nullable
    private ASTNode findChildByTypes(IElementType[] types) {
        return findChildByTypes(getNode(), types);
    }

    @Nullable
    private static ASTNode findChildByTypes(ASTNode node, IElementType[] types) {
        for (var type : types) {
            var child = node.findChildByType(type);
            if (child != null) {
                return child;
            }
        }
        return null;
    }
}
