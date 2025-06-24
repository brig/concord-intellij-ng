package brig.concord.documentation;

import brig.concord.meta.ConcordMetaTypeProvider;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;

import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class ConcordDocumentationProvider extends AbstractDocumentationProvider {

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        ConcordMetaTypeProvider metaTypeProvider = ConcordMetaTypeProvider.getInstance(element.getProject());
        YamlMetaType metaType = null;
        if (element instanceof YAMLKeyValue) {
            metaType = metaTypeProvider.getResolvedKeyValueMetaTypeMeta((YAMLKeyValue) element);
        } else if (element instanceof YAMLMapping) {
            metaType = metaTypeProvider.getResolvedMetaType(element);
        }

        if (metaType == null) {
            return null;
        }

        if (metaType instanceof Documented d) {
            return DEFINITION_START + metaType.getDisplayName() + DEFINITION_END +
                    CONTENT_START + d.getDescription() + CONTENT_END;
        }
        return null;
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(@NotNull Editor editor,
                                                              @NotNull PsiFile file,
                                                              @Nullable PsiElement contextElement,
                                                              int targetOffset) {
        IElementType elementType = PsiUtilCore.getElementType(contextElement);
        if (elementType == YAMLTokenTypes.SCALAR_KEY) {
            return contextElement.getParent();
        }
        return contextElement;
    }
}
