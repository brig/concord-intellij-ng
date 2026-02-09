package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class ImportsHighlightingTest extends HighlightingTestBase {

    @Test
    void testImportsSection() {
        configureFromText("""
            imports:
              - git:
                  url: "https://example.com/repo.git"
            """);

        highlight(key("imports")).is(ConcordHighlightingColors.DSL_SECTION);
    }

    @Test
    void testGitImportType() {
        configureFromResource("/highlighting/git-import.concord.yaml");

        highlight(key("/imports[0]/git")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(key("/imports[0]/git/url")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/git/path")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/git/version")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/git/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/git/dest")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/imports[0]/git/exclude")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/imports[0]/git/secret")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/git/secret/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/git/secret/org")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/git/secret/password")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testMvnImportType() {
        configureFromText("""
            imports:
              - mvn:
                  url: "mvn://com.example:tasks:1.0.0"
                  dest: "/"
            """);

        highlight(key("/imports[0]/mvn")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(key("/imports[0]/mvn/url")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/mvn/dest")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testDirImportType() {
        configureFromText("""
            imports:
              - dir:
                  src: "/1"
                  dest: "/2"
            """);

        highlight(key("/imports[0]/dir")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(key("/imports[0]/dir/src")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/imports[0]/dir/dest")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testMultipleImports() {
        configureFromText("""
            imports:
              - git:
                  url: "https://github.com/example/common.git"
                  version: "main"
              - mvn:
                  url: "mvn://com.example:tasks:2.0.0"
              - git:
                  url: "https://github.com/example/utils.git"
                  path: "flows"
            """);

        highlight(key("/imports[0]/git")).is(ConcordHighlightingColors.DSL_KIND);
        highlight(key("/imports[1]/mvn")).is(ConcordHighlightingColors.DSL_KIND);
        highlight(key("/imports[2]/git")).is(ConcordHighlightingColors.DSL_KIND);
    }
}
