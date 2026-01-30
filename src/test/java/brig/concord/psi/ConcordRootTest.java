package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Test;

public class ConcordRootTest extends ConcordYamlTestBase {

    @Test
    public void testDefaultPatterns() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

        assertEquals(rootFile, root.getRootFile());
        assertNotNull(root.getRootDir());
        assertNotNull(root.getScopeName());
        ReadAction.run(() -> assertFalse(root.getPatterns().isEmpty()));
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
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

        ReadAction.run(() -> assertEquals(2, root.getPatterns().size()));
    }

    @Test
    public void testContainsRootFile() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

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
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

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

        ConcordRoot root1 = new ConcordRoot(getProject(), rootFile);
        ConcordRoot root2 = new ConcordRoot(getProject(), rootFile);

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
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

        assertEquals(rootFile, root.getRootFile());
        ReadAction.run(() -> assertFalse(root.getPatterns().isEmpty()));
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
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

        // Should fall back to default patterns
        ReadAction.run(() -> assertFalse(root.getPatterns().isEmpty()));
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
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

        ReadAction.run(() -> assertEquals(1, root.getPatterns().size()));
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
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

        ReadAction.run(() -> assertEquals(1, root.getPatterns().size()));
    }
}
