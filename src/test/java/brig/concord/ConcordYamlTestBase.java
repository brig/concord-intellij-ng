package brig.concord;

import brig.concord.lexer.FlowDocTokenTypes;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.ArrayList;

public abstract class ConcordYamlTestBase extends BasePlatformTestCase {

    protected ConcordYamlPath yamlPath;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected PsiFile createFile(String path, String content) {
        return myFixture.addFileToProject(path, content);
    }

    protected YAMLFile configureFromText(@NotNull String content) {
        var file = myFixture.configureByText(ConcordFileType.INSTANCE, content);
        var yaml = Assertions.assertInstanceOf(YAMLFile.class, file);
        yamlPath = new ConcordYamlPath(yaml);
        return yaml;
    }

    protected PsiFile configureFromResource(@NotNull String resourcePath) {
        return configureFromText(loadResource(resourcePath));
    }

    protected void configureFromExistingFile(@NotNull PsiFile file) {
        myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
        var yaml = Assertions.assertInstanceOf(YAMLFile.class, file);
        yamlPath = new ConcordYamlPath(yaml);
    }

    protected void openFileInEditor(@NotNull PsiFile file) {
        EdtTestUtil.runInEdtAndWait(() ->
                myFixture.openFileInEditor(file.getVirtualFile())
        );
        yamlPath = new ConcordYamlPath((YAMLFile)file);
    }

    protected @NotNull AbstractTarget doc() {
        return new AbstractTarget("/") {
            @Override
            public @NotNull String text() {
                return ReadAction.compute(() -> document().getText());
            }

            @Override
            public @NotNull TextRange range() {
                return ReadAction.compute(() -> {
                    var doc = myFixture.getEditor().getDocument();
                    return TextRange.from(0, doc.getTextLength());
                });
            }
        };
    }

    protected void moveCaretTo(@NotNull AbstractTarget target) {
        EdtTestUtil.runInEdtAndWait(() -> {
            var offset = target.range().getStartOffset();
            Assertions.assertTrue( offset >= 0, target + " not found in test file");
            myFixture.getEditor().getCaretModel().moveToOffset(offset);
        });
    }

    protected @NotNull KeyTarget key(@NotNull String path) {
        if (yamlPath == null) {
            fail("yamlPath is null. Did you call configureFromText(...) ?");
        }
        return new KeyTarget(path, 1);
    }

    protected @NotNull KeyTarget key(@NotNull String path, int occurrence) {
        if (yamlPath == null) {
            fail("yamlPath is null. Did you call configureFromText(...) ?");
        }
        if (occurrence < 1) {
            fail("occurrence must be >= 1");
        }
        return new KeyTarget(path, occurrence);
    }

    protected @NotNull ValueTarget value(@NotNull String path) {
        if (yamlPath == null) {
            fail("yamlPath is null. Did you call configureFromText(...) ?");
        }
        return new ValueTarget(path);
    }

    private static @NotNull String loadResource(@NotNull String path) {
        try (var stream = ConcordYamlTestBase.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract class AbstractTarget {

        protected final String path;

        protected AbstractTarget(String path) {
            this.path = path;
        }

        /** Get the text of this target */
        public abstract @NotNull String text();

        /** Get the text range in the document */
        public abstract @NotNull TextRange range();

        public @NotNull String path() {
            return path;
        }

        @Override
        public String toString() {
            return path;
        }
    }

    public final class KeyTarget extends AbstractTarget {

        private final int occurrence;

        private KeyTarget(String path, int occurrence) {
            super(path);
            this.occurrence = occurrence;
        }

        @Override
        public @NotNull String text() {
            if (occurrence == 1) {
                return yamlPath.keyText(path);
            } else {
                return ReadAction.compute(() -> keyValue(path, occurrence).getKeyText());
            }
        }

        @Override
        public @NotNull TextRange range() {
            int start;
            if (occurrence == 1) {
                start = yamlPath.keyStartOffset(path);
            } else {
                start = keyStartOffset();
            }
            return TextRange.from(start, text().length());
        }

        public @NotNull PsiElement element() {
            return yamlPath.keyElement(path);
        }

        public @NotNull YAMLKeyValue asKeyValue() {
            var psi = yamlPath.keyElement(path).getParent();
            Assertions.assertInstanceOf(YAMLKeyValue.class, psi);
            return (YAMLKeyValue) psi;
        }

        private  int keyStartOffset() {
            return ReadAction.compute(() -> {
                var key = keyValue(path, occurrence).getKey();
                if (key == null) {
                    throw new AssertionError("Null key for path: " + path);
                }
                return key.getTextRange().getStartOffset();
            });
        }

        private @NotNull YAMLKeyValue keyValue(String path, int occurrence) {
            var split = splitParentAndName(path);
            var parentPath = split.parentPath;
            var name = split.name;

            var parentValue = yamlPath.valueElement(parentPath);
            if (!(parentValue instanceof YAMLMapping mapping)) {
                throw new AssertionError("Expected mapping at parent path: " + parentPath + " (from " + path + ")");
            }

            var seen = 0;
            for (var kv : mapping.getKeyValues()) {
                if (name.equals(kv.getKeyText())) {
                    seen++;
                    if (seen == occurrence) {
                        return kv;
                    }
                }
            }

            throw new AssertionError("Key '" + name + "' occurrence #" + occurrence + " not found under " + path);
        }

        private static Split splitParentAndName(String fullPath) {
            var p = fullPath.trim();
            if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
            int last = p.lastIndexOf('/');
            if (last < 0 || last == p.length() - 1) {
                throw new AssertionError("Invalid key path: " + fullPath + " (expected /.../name)");
            }
            var parent = (last == 0) ? "" : p.substring(0, last);
            var name = p.substring(last + 1);
            return new Split(parent, name);
        }

        private record Split(String parentPath, String name) {}
    }

    protected final class ValueTarget extends AbstractTarget {

        private ValueTarget(String path) {
            super(path);
        }

        @Override
        public @NotNull String text() {
            return document().getText(range());
        }

        @Override
        public @NotNull TextRange range() {
            return yamlPath.valueRange(path);
        }

        public @NotNull PsiElement element() {
            return yamlPath.valueElement(path);
        }

        public @NotNull SubstringTarget substring(@NotNull String needle) {
            var vr = range();
            var valueText = document().getText(vr);

            var rel = valueText.indexOf(needle);
            if (rel < 0) {
                fail("Substring '" + needle + "' not found in value at path '" + path + "'.\n" +
                        "Value text: " + valueText);
            }
            return new SubstringTarget(path, needle, vr.getStartOffset() + rel);
        }
    }

    protected static final class SubstringTarget extends AbstractTarget {
        private final String needle;
        private final int offset;

        private SubstringTarget(String path, String needle, int offset) {
            super(path);
            this.needle = needle;
            this.offset = offset;
        }

        @Override
        public @NotNull String text() {
            return needle;
        }

        @Override
        public @NotNull TextRange range() {
            return TextRange.from(offset, needle.length());
        }
    }

    private @NotNull Document document() {
        return myFixture.getEditor().getDocument();
    }

    /**
     * Get flow documentation target for the flow at the given path.
     * @param flowPath path to the flow key (e.g., "/flows/myFlow")
     * @return target for the FlowDocumentation element before this flow
     */
    protected @NotNull FlowDocTarget flowDoc(@NotNull String flowPath) {
        return new FlowDocTarget(flowPath);
    }

    /**
     * Get the nth FlowDocumentation element in the file (0-indexed).
     * Useful for testing orphaned documentation.
     */
    protected @NotNull FlowDocByIndexTarget flowDocByIndex(int index) {
        return new FlowDocByIndexTarget(index);
    }

    /**
     * Get a parameter target from flow documentation.
     * @param flowPath path to the flow key
     * @param paramName name of the parameter
     * @return target for the FlowDocParameter element
     */
    protected @NotNull FlowDocParamTarget flowDocParam(@NotNull String flowPath, @NotNull String paramName) {
        return new FlowDocParamTarget(flowPath, paramName);
    }

    /**
     * Get a parameter target by occurrence (for duplicates).
     * @param flowPath path to the flow key
     * @param paramName name of the parameter
     * @param occurrence 1-based occurrence index
     * @return target for the FlowDocParameter element
     */
    protected @NotNull FlowDocParamTarget flowDocParam(@NotNull String flowPath, @NotNull String paramName, int occurrence) {
        return new FlowDocParamTarget(flowPath, paramName, occurrence);
    }

    /**
     * Get target for unknown keyword in flow doc parameter.
     * @param flowPath path to the flow key
     * @param paramName name of the parameter
     * @return target for the FLOW_DOC_UNKNOWN_KEYWORD token
     */
    protected @NotNull UnknownKeywordTarget unknownKeyword(@NotNull String flowPath, @NotNull String paramName) {
        return new UnknownKeywordTarget(flowPath, paramName);
    }

    public final class FlowDocTarget extends AbstractTarget {

        private FlowDocTarget(String flowPath) {
            super(flowPath);
        }

        @Override
        public @NotNull String text() {
            return ReadAction.compute(() -> getFlowDoc().getText());
        }

        @Override
        public @NotNull TextRange range() {
            return ReadAction.compute(() -> getFlowDoc().getTextRange());
        }

        public @NotNull FlowDocumentation getFlowDoc() {
            var kv = yamlPath.keyElement(path).getParent();
            var sibling = kv.getPrevSibling();
            while (sibling != null && !(sibling instanceof FlowDocumentation)) {
                sibling = sibling.getPrevSibling();
            }
            if (sibling == null) {
                throw new AssertionError("FlowDocumentation not found before: " + path);
            }
            return (FlowDocumentation) sibling;
        }
    }

    public final class FlowDocParamTarget extends AbstractTarget {

        private final String paramName;
        private final int occurrence;

        private FlowDocParamTarget(String flowPath, String paramName) {
            this(flowPath, paramName, 1);
        }

        private FlowDocParamTarget(String flowPath, String paramName, int occurrence) {
            super(flowPath);
            this.paramName = paramName;
            this.occurrence = occurrence;
        }

        @Override
        public @NotNull String text() {
            return ReadAction.compute(() -> getParam().getText());
        }

        @Override
        public @NotNull TextRange range() {
            return ReadAction.compute(() -> getParam().getTextRange());
        }

        public @NotNull FlowDocParameter getParam() {
            var doc = new FlowDocTarget(path).getFlowDoc();
            var allParams = new ArrayList<FlowDocParameter>();
            allParams.addAll(doc.getInputParameters());
            allParams.addAll(doc.getOutputParameters());

            var matching = allParams.stream()
                    .filter(p -> paramName.equals(p.getName()))
                    .toList();

            if (matching.isEmpty()) {
                throw new AssertionError("Parameter '" + paramName + "' not found in flow doc for: " + path);
            }
            if (occurrence > matching.size()) {
                throw new AssertionError("Parameter '" + paramName + "' occurrence #" + occurrence +
                        " not found (only " + matching.size() + " found)");
            }
            return matching.get(occurrence - 1);
        }
    }

    public class FlowDocByIndexTarget extends AbstractTarget {

        private final int index;

        private FlowDocByIndexTarget(int index) {
            super("flowDoc[" + index + "]");
            this.index = index;
        }

        @Override
        public @NotNull String text() {
            return ReadAction.compute(() -> getFlowDoc().getText());
        }

        @Override
        public @NotNull TextRange range() {
            return ReadAction.compute(() -> getFlowDoc().getTextRange());
        }

        public @NotNull FlowDocumentation getFlowDoc() {
            var docs = PsiTreeUtil.findChildrenOfType(myFixture.getFile(), FlowDocumentation.class);
            var list = new ArrayList<>(docs);
            if (index >= list.size()) {
                throw new AssertionError("FlowDocumentation at index " + index + " not found (only " + list.size() + " found)");
            }
            return list.get(index);
        }
    }

    public final class UnknownKeywordTarget extends AbstractTarget {

        private final String paramName;

        private UnknownKeywordTarget(String flowPath, String paramName) {
            super(flowPath);
            this.paramName = paramName;
        }

        @Override
        public @NotNull String text() {
            return ReadAction.compute(() -> {
                var node = getUnknownKeywordNode();
                return node.getText();
            });
        }

        @Override
        public @NotNull TextRange range() {
            return ReadAction.compute(() -> {
                var node = getUnknownKeywordNode();
                return node.getTextRange();
            });
        }

        private @NotNull com.intellij.lang.ASTNode getUnknownKeywordNode() {
            var param = new FlowDocParamTarget(path, paramName).getParam();
            var node = param.getNode().findChildByType(FlowDocTokenTypes.FLOW_DOC_UNKNOWN_KEYWORD);
            if (node == null) {
                throw new AssertionError("FLOW_DOC_UNKNOWN_KEYWORD not found in parameter '" + paramName + "'");
            }
            return node;
        }
    }
}
