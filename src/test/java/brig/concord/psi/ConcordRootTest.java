package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcordRootTest extends ConcordYamlTestBaseJunit5 {

    @Test
    public void testDefaultPatterns() {
        var yaml = configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        VirtualFile rootFile = yaml.getVirtualFile();
        ConcordRoot root = new ConcordRoot(getProject(), rootFile);

        Assertions.assertEquals(rootFile, root.getRootFile());
        Assertions.assertNotNull(root.getRootDir());
        Assertions.assertNotNull(root.getScopeName());
        ReadAction.run(() -> Assertions.assertFalse(root.getPatterns().isEmpty()));
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

        ReadAction.run(() -> Assertions.assertEquals(2, root.getPatterns().size()));
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
        ReadAction.run(() -> Assertions.assertTrue(root.contains(rootFile)));
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
        Assertions.assertNotNull(root.getScopeName());
        Assertions.assertFalse(root.getScopeName().isEmpty());
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

        Assertions.assertEquals(root1, root2);
        Assertions.assertEquals(root1.hashCode(), root2.hashCode());
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

        Assertions.assertEquals(rootFile, root.getRootFile());
        ReadAction.run(() -> Assertions.assertFalse(root.getPatterns().isEmpty()));
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
        ReadAction.run(() -> Assertions.assertFalse(root.getPatterns().isEmpty()));
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

        ReadAction.run(() -> Assertions.assertEquals(1, root.getPatterns().size()));
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

        ReadAction.run(() -> Assertions.assertEquals(1, root.getPatterns().size()));
    }
}
