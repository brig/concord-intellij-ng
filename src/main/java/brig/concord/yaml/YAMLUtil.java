package brig.concord.yaml;

import brig.concord.ConcordBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.*;
import brig.concord.yaml.psi.impl.YAMLBlockMappingImpl;

import java.util.Locale;
import java.util.function.Supplier;

public final class YAMLUtil {

    private static final TokenSet BLANK_LINE_ELEMENTS = TokenSet.andNot(YAMLElementTypes.BLANK_ELEMENTS, YAMLElementTypes.EOL_ELEMENTS);

    public static boolean isNumberValue(@NotNull String originalValue) {
        if (originalValue.isEmpty()) {
            return false;
        }

        var value = originalValue.toLowerCase(Locale.ROOT);
        var first = value.charAt(0);

        // Check special YAML number literals first
        if (".inf".equals(value) || "-.inf".equals(value) || "+.inf".equals(value) || ".nan".equals(value)) {
            return true;
        }

        // Check hex/octal
        if (value.startsWith("0x") || value.startsWith("0o")) {
            return true;
        }

        // Try to parse as a number
        if (Character.isDigit(first) || first == '-' || first == '+' || first == '.') {
            // For values starting with '.', check next char is digit
            if (first == '.' && (value.length() <= 1 || !Character.isDigit(value.charAt(1)))) {
                return false;
            }
            try {
                Double.parseDouble(originalValue);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    public static PsiElement rename(final YAMLKeyValue element, final String newName) {
        if (newName.equals(element.getName())) {
            throw new IncorrectOperationException(ConcordBundle.message("rename.same.name"));
        }
        final YAMLKeyValue topKeyValue = YAMLElementGenerator.getInstance(element.getProject()).createYamlKeyValue(newName, "Foo");

        final PsiElement key = element.getKey();
        if (key == null || topKeyValue.getKey() == null) {
            throw new IllegalStateException();
        }
        key.replace(topKeyValue.getKey());
        return element;
    }

    public static int getIndentInThisLine(final @NotNull PsiElement elementInLine) {
        PsiElement currentElement = elementInLine;
        while (currentElement != null) {
            final IElementType type = currentElement.getNode().getElementType();
            if (type == YAMLTokenTypes.EOL) {
                return 0;
            }
            if (type == YAMLTokenTypes.INDENT) {
                return currentElement.getTextLength();
            }

            currentElement = PsiTreeUtil.prevLeaf(currentElement);
        }
        return 0;
    }

    public static int getIndentToThisElement(@NotNull PsiElement element) {
        if (element instanceof YAMLBlockMappingImpl) {
            try {
                element = ((YAMLBlockMappingImpl)element).getFirstKeyValue();
            } catch (IllegalStateException e) {
                // Spring Boot plug-in modifies PSI-tree into invalid state
                // This is a workaround over EA-133507 IDEA-210113
                if (!e.getMessage().equals(YAMLBlockMappingImpl.EMPTY_MAP_MESSAGE)) {
                    throw e;
                }
                else {
                    Logger.getInstance(YAMLUtil.class).error(YAMLBlockMappingImpl.EMPTY_MAP_MESSAGE);
                }
            }
        }
        int offset = element.getTextOffset();

        PsiElement currentElement = element;
        while (currentElement != null) {
            final IElementType type = currentElement.getNode().getElementType();
            if (YAMLElementTypes.EOL_ELEMENTS.contains(type)) {
                return offset - currentElement.getTextOffset() - currentElement.getTextLength();
            }

            currentElement = PsiTreeUtil.prevLeaf(currentElement);
        }
        return offset;
    }

    public static boolean psiAreAtTheSameLine(@NotNull PsiElement psi1, @NotNull PsiElement psi2) {
        PsiElement leaf = firstLeaf(psi1);
        PsiElement lastLeaf = firstLeaf(psi2);
        while (leaf != null) {
            if (PsiUtilCore.getElementType(leaf) == YAMLTokenTypes.EOL) {
                return false;
            }
            if (leaf == lastLeaf) {
                return true;
            }
            leaf = PsiTreeUtil.nextLeaf(leaf);
        }
        // It is a kind of magic, normally we should return from the `while` above
        return false;
    }

    private static @Nullable PsiElement firstLeaf(PsiElement psi1) {
        LeafElement leaf = TreeUtil.findFirstLeaf(psi1.getNode());
        if (leaf != null) {
            return leaf.getPsi();
        }
        else {
            return null;
        }
    }


    /**
     * Deletes surrounding whitespace contextually. First attempts to delete {@link YAMLTokenTypes#COMMENT}s on the same line and
     * {@link YAMLElementTypes#SPACE_ELEMENTS} forward, otherwise it will delete {@link YAMLElementTypes#SPACE_ELEMENTS} backward.
     * <p>
     * This is useful for maintaining consistent formatting.
     * <p>
     * E.g.,
     * <pre>{@code
     * foo:
     *   bar: value1 # Same line comment
     *   # Next line comment
     *   baz: value2
     * }</pre>
     * becomes
     * <pre>{@code
     * foo:
     *   bar: value1 # Next line comment
     *   baz: value2
     * }</pre>
     */
    public static void deleteSurroundingWhitespace(final @NotNull PsiElement element) {
        if (element.getNextSibling() != null) {
            deleteElementsOfType(element::getNextSibling, BLANK_LINE_ELEMENTS);
            deleteElementsOfType(element::getNextSibling, YAMLElementTypes.SPACE_ELEMENTS);
        }
        else {
            deleteElementsOfType(element::getPrevSibling, YAMLElementTypes.SPACE_ELEMENTS);
        }
    }

    private static void deleteElementsOfType(final @NotNull Supplier<? extends PsiElement> element, final @NotNull TokenSet types) {
        while (element.get() != null && types.contains(PsiUtilCore.getElementType(element.get()))) {
            element.get().delete();
        }
    }
}

