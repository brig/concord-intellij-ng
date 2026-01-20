package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import brig.concord.yaml.psi.YAMLDocument;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

public class ConcordRootTest extends ConcordYamlTestBase {

    @Test
    public void testDefaultPatterns() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root = new ConcordRoot(rootFile, doc);

        assertEquals(rootFile, root.getRootFile());
        assertNotNull(root.getRootDir());
        assertNotNull(root.getScopeName());
        assertFalse(root.getPatterns().isEmpty());
    }

    @Test
    public void testCustomResourcesPatterns() {
        var yaml = configureFromText("""
                resources:
                  concord:
                    - "glob:src/**/*.concord.yaml"
                    - "glob:lib/**/*.concord.yml"
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root = new ConcordRoot(rootFile, doc);

        assertEquals(2, root.getPatterns().size());
    }

    @Test
    public void testContainsRootFile() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root = new ConcordRoot(rootFile, doc);

        // Root file should always be contained in its own scope
        assertTrue(root.contains(rootFile));
    }

    @Test
    public void testScopeName() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root = new ConcordRoot(rootFile, doc);

        // Scope name should be the parent directory name
        assertNotNull(root.getScopeName());
        assertFalse(root.getScopeName().isEmpty());
    }

    @Test
    public void testEquality() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root1 = new ConcordRoot(rootFile, doc);
        ConcordRoot root2 = new ConcordRoot(rootFile, doc);

        assertEquals(root1, root2);
        assertEquals(root1.hashCode(), root2.hashCode());
    }

    @Test
    public void testNullDocument() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();

        // Should not throw when document is null
        ConcordRoot root = new ConcordRoot(rootFile, null);

        assertEquals(rootFile, root.getRootFile());
        assertFalse(root.getPatterns().isEmpty());
    }

    @Test
    public void testEmptyResourcesConcord() {
        var yaml = configureFromText("""
                resources:
                  concord: []
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root = new ConcordRoot(rootFile, doc);

        // Should fall back to default patterns
        assertFalse(root.getPatterns().isEmpty());
    }

    @Test
    public void testRegexPattern() {
        var yaml = configureFromText("""
                resources:
                  concord:
                    - "regex:.*/flows/.*\\.concord\\.yaml"
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root = new ConcordRoot(rootFile, doc);

        assertEquals(1, root.getPatterns().size());
    }

    @Test
    public void testPlainFilePattern() {
        var yaml = configureFromText("""
                resources:
                  concord:
                    - "flows/main.concord.yaml"
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        YAMLDocument doc = PsiTreeUtil.getChildOfType(yaml, YAMLDocument.class);

        ConcordRoot root = new ConcordRoot(rootFile, doc);

        assertEquals(1, root.getPatterns().size());
    }
}
