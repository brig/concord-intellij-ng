package brig.concord;

import brig.concord.psi.ConcordYamlPath;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

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

    protected YAMLFile configureFromText(@NotNull String content) {
        var file = myFixture.configureByText(ConcordFileType.INSTANCE, content);
        var yaml = Assertions.assertInstanceOf(YAMLFile.class, file);
        yamlPath = new ConcordYamlPath(yaml);
        return yaml;
    }

    protected PsiFile configureFromResource(@NotNull String resourcePath) {
        return configureFromText(loadResource(resourcePath));
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
    }

    protected final class KeyTarget extends AbstractTarget {

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
}
