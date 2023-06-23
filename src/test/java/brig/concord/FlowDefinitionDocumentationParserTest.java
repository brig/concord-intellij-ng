package brig.concord;

import brig.concord.documentation.FlowDefinitionDocumentationParser;
import brig.concord.documentation.FlowDocumentation;
import brig.concord.documentation.ParamType;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlowDefinitionDocumentationParserTest {

    @Test
    public void testParseOK() {
        String str = """
                ##
                # Flow Caption.
                # out:
                #   testOut: boom
                # in:
                #   p1: string, mandatory, test param1
                ##
                """;

        FlowDocumentation doc = FlowDefinitionDocumentationParser.parse(toCommentElements(str));
        assertEquals("Flow Caption.", doc.description());
        assertEquals(1, doc.in().list().size());
        assertEquals("p1", doc.in().list().get(0).name());
        assertEquals(ParamType.STRING, doc.in().list().get(0).type());
        assertEquals(1, doc.out().size());
        assertEquals("testOut", doc.out().get(0).name());
    }

    @Test
    public void testParseEmpty() {
        String str = """
                """;

        FlowDocumentation doc = FlowDefinitionDocumentationParser.parse(toCommentElements(str));
        assertEquals("", doc.description());
        assertEquals(0, doc.in().list().size());
        assertEquals(0, doc.out().size());
    }

    @Test
    public void testParseInvalidCommentFormat() {
        String str = """
                #
                # in:
                #   a: test
                """;

        FlowDocumentation doc = FlowDefinitionDocumentationParser.parse(toCommentElements(str));
        assertEquals("", doc.description());
        assertEquals(0, doc.in().list().size());
        assertEquals(0, doc.out().size());
    }

    private static PsiComment toCommentElements(String str) {
        String[] lines = str.split("\n");

        MyComment result = new MyComment(lines[0]);

        MyComment current = result;
        for (int i = 1; i < lines.length; i++) {
            MyComment sibling = new MyComment(lines[i]);
            current.nextSibling = sibling;
            current = sibling;
        }
        return result;
    }

    static class MyComment extends PsiCommentImpl {

        private PsiElement nextSibling;

        public MyComment(@NotNull String text) {
            super(YAMLTokenTypes.COMMENT, text);
        }

        @Override
        public PsiElement getNextSibling() {
            return nextSibling;
        }
    }
}
