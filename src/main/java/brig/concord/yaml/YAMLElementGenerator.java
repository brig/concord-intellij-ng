package brig.concord.yaml;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import brig.concord.ConcordFileType;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.*;
import brig.concord.yaml.psi.impl.YAMLQuotedTextImpl;

import java.util.Collection;

public class YAMLElementGenerator {
    private final Project myProject;

    public YAMLElementGenerator(Project project) {
        myProject = project;
    }

    public static YAMLElementGenerator getInstance(Project project) {
        return project.getService(YAMLElementGenerator.class);
    }

    public @NotNull YAMLKeyValue createYamlKeyValue(@NotNull String keyName, @NotNull String valueText) {
        final PsiFile tempValueFile = createDummyYamlWithText(valueText);
        Collection<YAMLValue> values = PsiTreeUtil.collectElementsOfType(tempValueFile, YAMLValue.class);

        String text;
        if (values.isEmpty()) {
            text = keyName + ":";
        }
        else if (values.iterator().next() instanceof YAMLScalar && !valueText.contains("\n")) {
            text = keyName + ": " + valueText;
        }
        else {
            text = keyName + ":\n" + YAMLTextUtil.indentText(valueText, 2);
        }

        final PsiFile tempFile = createDummyYamlWithText(text);
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLKeyValue.class).iterator().next();
    }

    public @NotNull YAMLQuotedTextImpl createYamlDoubleQuotedString() {
        final YAMLFile tempFile = createDummyYamlWithText("\"foo\"");
        return PsiTreeUtil.collectElementsOfType(tempFile, YAMLQuotedTextImpl.class).iterator().next();
    }

    public @NotNull YAMLFile createDummyYamlWithText(@NotNull String text) {
        return (YAMLFile) PsiFileFactory.getInstance(myProject)
                .createFileFromText("temp." + ConcordFileType.INSTANCE.getDefaultExtension(), ConcordFileType.INSTANCE, text, LocalTimeCounter.currentTime(), false);
    }

    public @NotNull PsiElement createEol() {
        final YAMLFile file = createDummyYamlWithText("\n");
        return PsiTreeUtil.getDeepestFirst(file);
    }

    public @NotNull PsiElement createSpace() {
        final YAMLKeyValue keyValue = createYamlKeyValue("foo", "bar");
        final ASTNode whitespaceNode = keyValue.getNode().findChildByType(TokenType.WHITE_SPACE);
        assert whitespaceNode != null;
        return whitespaceNode.getPsi();
    }

    public @NotNull PsiElement createIndent(int size) {
        final YAMLFile file = createDummyYamlWithText(StringUtil.repeatSymbol(' ', size));
        return PsiTreeUtil.getDeepestFirst(file);
    }

    public @NotNull PsiElement createColon() {
        final YAMLFile file = createDummyYamlWithText("? foo : bar");
        final PsiElement at = file.findElementAt("? foo ".length());
        assert at != null && at.getNode().getElementType() == YAMLTokenTypes.COLON;
        return at;
    }

    public @NotNull YAMLSequenceItem createSequenceItem(@NotNull String valueText) {
        final YAMLFile file = createDummyYamlWithText("- " + valueText);
        return PsiTreeUtil.collectElementsOfType(file, YAMLSequenceItem.class).iterator().next();
    }

    public @NotNull YAMLScalar createYamlScalar(@NotNull String text) {
        final YAMLFile file = createDummyYamlWithText("foo: " + text);
        YAMLKeyValue kv = PsiTreeUtil.collectElementsOfType(file, YAMLKeyValue.class).iterator().next();
        return (YAMLScalar) kv.getValue();
    }

    public @NotNull FlowDocParameter createFlowDocParameter(@NotNull String name, @NotNull String type) {
        var dummyYaml = """
            flows:
              ##
              # in:
              #   %s: %s
              ##
              dummy:
                - log: "test"
            """.formatted(name, type);
        var dummyFile = createDummyYamlWithText(dummyYaml);

        var dummyDoc = PsiTreeUtil.findChildOfType(dummyFile, FlowDocumentation.class);
        if (dummyDoc == null) {
            throw new IncorrectOperationException("Failed to create dummy flow documentation");
        }

        var dummyParams = dummyDoc.getInputParameters();
        if (dummyParams.isEmpty()) {
            throw new IncorrectOperationException("Failed to create dummy parameter");
        }

        return dummyParams.getFirst();
    }
}