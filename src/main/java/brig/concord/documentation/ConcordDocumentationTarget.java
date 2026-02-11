package brig.concord.documentation;

import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class ConcordDocumentationTarget implements DocumentationTarget {

    private final SmartPsiElementPointer<PsiElement> elementPointer;
    private final Documented documented;
    private final String typeName;

    public ConcordDocumentationTarget(PsiElement element, Documented documented, String typeName) {
        this.elementPointer = SmartPointerManager.createPointer(element);
        this.documented = documented;
        this.typeName = typeName;
    }

    @Override
    public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
        var ptr = this.elementPointer;
        var documented = this.documented;
        var typeName = this.typeName;
        return () -> {
            var element = ptr.getElement();
            return element == null ? null : new ConcordDocumentationTarget(element, documented, typeName);
        };
    }

    @Override
    public @NotNull TargetPresentation computePresentation() {
        return TargetPresentation.builder(typeName).presentation();
    }

    @Override
    public @Nullable String computeDocumentationHint() {
        return documented.getDescription();
    }

    @Override
    public @Nullable DocumentationResult computeDocumentation() {
        var element = elementPointer.getElement();
        if (element == null) {
            return null;
        }

        var sb = new StringBuilder();

        sb.append(DEFINITION_START).append(toName(element)).append(DEFINITION_END);

        sb.append(CONTENT_START);

        var customBody = documented.getDocumentationBody(element);
        if (customBody != null) {
            sb.append(customBody);
        } else {
            sb.append("<p>Type: <code>").append(typeName).append("</code></p>");

            var description = documented.getDescription();
            if (description != null) {
                sb.append("<p>").append(description).append("</p>");
            }

            var keysList = buildKeysList(documented.getDocumentationFields());
            if (keysList != null) {
                sb.append(keysList);
            }

            var example = documented.getDocumentationExample();
            if (example != null) {
                sb.append("<p><b>Example:</b></p>")
                        .append("<pre><code>")
                        .append(example)
                        .append("</code></pre>");
            }
        }

        sb.append(CONTENT_END);

        return DocumentationResult.documentation(sb.toString());
    }

    private static String toName(PsiElement element) {
        if (element instanceof YAMLKeyValue kv) {
            return kv.getKeyText();
        }
        return "N/A";
    }

    private static @Nullable String buildKeysList(List<Documented.DocumentedField> fields) {
        if (fields.isEmpty()) {
            return null;
        }

        var sb = new StringBuilder();

        sb.append("<p><b>Keys:</b></p>");
        sb.append("<ul>");
        for (var f : fields) {
            sb.append("<li><code>").append(f.name()).append("</code>");
            if (f.typeDisplayName() != null) {
                sb.append(" <i>(").append(f.typeDisplayName());
                if (f.required()) {
                    sb.append(", required");
                }
                sb.append(")</i>");
            }
            if (f.description() != null) {
                sb.append(" &mdash; ").append(normalizeDescription(f.description()));
            }
            if (!f.children().isEmpty()) {
                sb.append("<ul>");
                for (var child : f.children()) {
                    sb.append("<li><code>").append(child.name()).append("</code>");
                    if (child.description() != null) {
                        sb.append(" &mdash; ").append(child.description());
                    }
                    sb.append("</li>");
                }
                sb.append("</ul>");
            }
            sb.append("</li>");
        }
        sb.append("</ul>");

        return sb.toString();
    }

    private static String normalizeDescription(@NotNull String description) {
        return StringUtil.decapitalize(description);
    }
}
