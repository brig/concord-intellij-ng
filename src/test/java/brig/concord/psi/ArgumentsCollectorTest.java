package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentsCollectorTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void collectFromRootFile() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    myVar: "hello"
                    count: 42
                flows:
                  main:
                    - log: "hi"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        assertEquals(1, byScope.size());
        var args = byScope.values().iterator().next();
        assertEquals(2, args.size());
        assertTrue(args.containsKey("myVar"));
        assertTrue(args.containsKey("count"));
    }

    @Test
    void collectFromMultipleFilesInScope() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    rootVar: "fromRoot"
                resources:
                  concord:
                    - "glob:concord/*.concord.yaml"
                flows:
                  main:
                    - log: "hi"
                """);

        createFile("concord/extra.concord.yaml", """
                configuration:
                  arguments:
                    extraVar: "fromExtra"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        assertEquals(1, byScope.size());
        var args = byScope.values().iterator().next();
        assertEquals(2, args.size());
        assertTrue(args.containsKey("rootVar"));
        assertTrue(args.containsKey("extraVar"));
    }

    @Test
    void rootFileOverridesOthers() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    shared: "fromRoot"
                resources:
                  concord:
                    - "glob:concord/*.concord.yaml"
                flows:
                  main:
                    - log: "hi"
                """);

        createFile("concord/extra.concord.yaml", """
                configuration:
                  arguments:
                    shared: "fromExtra"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        var args = byScope.values().iterator().next();
        assertEquals(1, args.size());
        // Root file should win because it's processed last
        assertEquals("\"fromRoot\"", ReadAction.compute(() -> args.get("shared").getText()));
    }

    @Test
    void laterFileOverridesEarlierByName() {
        createFile("concord.yaml", """
                resources:
                  concord:
                    - "glob:concord/*.concord.yaml"
                flows:
                  main:
                    - log: "hi"
                """);

        // "aaa" sorts before "zzz"
        createFile("concord/aaa.concord.yaml", """
                configuration:
                  arguments:
                    shared: "fromAAA"
                """);

        createFile("concord/zzz.concord.yaml", """
                configuration:
                  arguments:
                    shared: "fromZZZ"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        var args = byScope.values().iterator().next();
        // zzz sorts after aaa, so zzz should win
        assertEquals("\"fromZZZ\"", ReadAction.compute(() -> args.get("shared").getText()));
    }

    @Test
    void emptyWhenNoArguments() {
        createFile("concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "hi"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        assertTrue(byScope.isEmpty());
    }

    @Test
    void emptyWhenNoConfiguration() {
        createFile("concord.yaml", """
                flows:
                  main:
                    - log: "hi"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        assertTrue(byScope.isEmpty());
    }

    @Test
    void getArgumentsForContext() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    myVar: "hello"
                flows:
                  main:
                    - log: "hi"
                """);

        var psiFile = configureFromText("""
                configuration:
                  arguments:
                    myVar: "hello"
                flows:
                  main:
                    - log: "hi"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var args = ReadAction.compute(() -> collector.getArguments(psiFile));

        assertTrue(args.containsKey("myVar"));
    }

    @Test
    void mergeFromMultipleScopeFiles() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    rootOnly: "r"
                    shared: "rootValue"
                resources:
                  concord:
                    - "glob:concord/*.concord.yaml"
                flows:
                  main:
                    - log: "hi"
                """);

        createFile("concord/a.concord.yaml", """
                configuration:
                  arguments:
                    aOnly: "a"
                    shared: "aValue"
                """);

        createFile("concord/b.concord.yaml", """
                configuration:
                  arguments:
                    bOnly: "b"
                    shared: "bValue"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        var args = byScope.values().iterator().next();
        assertEquals(4, args.size());
        assertTrue(args.containsKey("rootOnly"));
        assertTrue(args.containsKey("aOnly"));
        assertTrue(args.containsKey("bOnly"));
        // Root processed last — overrides a.concord.yaml and b.concord.yaml
        assertEquals("\"rootValue\"", ReadAction.compute(() -> args.get("shared").getText()));
    }

    @Test
    void fileWithoutArgumentsSectionIsSkipped() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    rootVar: "hello"
                resources:
                  concord:
                    - "glob:concord/*.concord.yaml"
                flows:
                  main:
                    - log: "hi"
                """);

        createFile("concord/no-args.concord.yaml", """
                flows:
                  helper:
                    - log: "help"
                """);

        var collector = ArgumentsCollector.getInstance(getProject());
        var byScope = ReadAction.compute(collector::collectByScope);

        var args = byScope.values().iterator().next();
        assertEquals(1, args.size());
        assertTrue(args.containsKey("rootVar"));
    }
}
