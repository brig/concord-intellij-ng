package brig.concord;

import brig.concord.psi.ConcordYamlPath;
import brig.concord.yaml.psi.YAMLFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    protected @NotNull KeyTarget key(@NotNull String path) {
        if (yamlPath == null) {
            fail("yamlPath is null. Did you call configureFromText(...) ?");
        }
        return new KeyTarget(path);
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

        /** Get the PSI element at this target (if available) */
        public @Nullable PsiElement psiElement() {
            return null;
        }

        public @NotNull String path() {
            return path;
        }
    }

    protected final class KeyTarget extends AbstractTarget {

        private KeyTarget(String path) {
            super(path);
        }

        @Override
        public @NotNull String text() {
            return yamlPath.keyText(path);
        }

        @Override
        public @NotNull TextRange range() {
            int start = yamlPath.keyStartOffset(path);
            return TextRange.from(start, text().length());
        }

        @Override
        public @Nullable PsiElement psiElement() {
            return yamlPath.keyElement(path);
        }
    }

    protected final class ValueTarget extends AbstractTarget {

        private ValueTarget(String path) {
            super(path);
        }

        @Override
        public @NotNull String text() {
            return doc().getText(range());
        }

        @Override
        public @NotNull TextRange range() {
            return yamlPath.valueRange(path);
        }

        @Override
        public @Nullable PsiElement psiElement() {
            return yamlPath.valueElement(path);
        }

        public @NotNull SubstringTarget substring(@NotNull String needle) {
            var vr = range();
            var valueText = doc().getText(vr);

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

    private @NotNull Document doc() {
        return myFixture.getEditor().getDocument();
    }
}
