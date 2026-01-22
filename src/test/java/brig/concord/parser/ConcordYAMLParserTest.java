package brig.concord.parser;

import brig.concord.ConcordYamlTestBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.source.tree.ForeignLeafPsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.testFramework.ParsingTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.intellij.testFramework.ParsingTestCase.*;

public class ConcordYAMLParserTest extends ConcordYamlTestBase {

    @Test
    public void testBlockMapping() {
        doTest("/parser/blockMapping.concord.yaml");
    }

    @Test
    public void testIndentMapping() {
        doTest("/parser/indentMapping.concord.yaml");
    }

    @Test
    public void testInlineMapWithBlockScalarValue() {
        doTest("/parser/InlineMapWithBlockScalarValue.concord.yaml");
    }

    @Test
    public void testErrorInBlockScalarHeader() {
        doTest("/parser/errorInBlockScalarHeader.concord.yaml", false);
    }

    @Test
    public void testCommentInBlockScalarHeader() {
        doTest("/parser/commentInBlockScalarHeader.concord.yaml");
    }

    @Test
    public void testScalarsWithNewlines() {
        doTest("/parser/scalarsWithNewlines.concord.yaml");
    }

    @Test
    public void testMultipleDocsWithMappings() {
        doTest("/parser/multipleDocsWithMappings.concord.yaml");
    }

    @Test
    public void testKeyValueWithEmptyLineAhead() {
        doTest("/parser/keyValueWithEmptyLineAhead.concord.yaml");
    }

    @Test
    public void testIncompleteKeyInHierarchy() {
        doTest("/parser/incompleteKeyInHierarchy.concord.yaml");
    }

    @Test
    public void testAnsibleRoleElkMain() {
        doTest("/parser/ansibleRoleElkMain.concord.yaml");
    }

    @Test
    public void testAnsibleRoleElkInit() {
        doTest("/parser/ansibleRoleElkInit.concord.yaml");
    }

    @Test
    public void testSpec2_27() {
        doTest("/parser/spec2_27.concord.yaml");
    }

    @Test
    public void testExplicitMaps() {
        doTest("/parser/explicitMaps.concord.yaml");
    }

    @Test
    public void testShiftedList() {
        doCodeTest("/parser/shiftedList.txt",
                "    - item1\n" +
                "    - item2"
                );
    }

    @Test
    public void testIncompleteKeyWithWhitespace() {
        doCodeTest("/parser/incompleteKeyWithWhitespace.txt", """
                 logging:
                   config:
                  \s
                  \s
                  \s
                  \s
                 """);
    }

    @Test
    public void testShiftedMap() {
        doCodeTest("/parser/shiftedMap.txt",
                "    key: ttt\n" +
                "    ahahah: ppp");
    }

    @Test
    public void testIncompleteKey() {
        doCodeTest("/parser/incompleteKey.txt", """
                 logging:
                   config: bla
                   index""");
    }

    @Test
    public void test2docs() {
        doCodeTest("/parser/2doc.txt", """
                 # Ranking of 1998 home runs
                 ---
                 - Mark McGwire
                 - Sammy Sosa
                 - Ken Griffey

                 # Team ranking
                 ---
                 - Chicago Cubs
                 - St Louis Cardinals""");
    }

    @Test
    public void testIndentation() {
        doCodeTest("/parser/indentation.txt", """
                 name: Mark McGwire
                 accomplishment: >
                   Mark set a major league
                   home run record in 1998.
                 stats: |
                   65 Home Runs
                   0.278 Batting Average""");
    }

    @Test
    public void testMap_between_seq() throws Throwable {
        doCodeTest("/parser/map_between_seq.txt", """
                 ?
                   - Detroit Tigers
                   - Chicago cubs
                 :
                   - 2001-07-23

                 ? [ New York Yankees,
                     Atlanta Braves ]
                 : [ 2001-07-02, 2001-08-12,
                     2001-08-14 ]""");
    }

    @Test
    public void testMap_map() {
        doCodeTest("/parser/map_map.txt", """
                 Mark McGwire: {hr: 65, avg: 0.278}
                 Sammy Sosa: {
                     hr: 63,
                     avg: 0.288
                   }""");
    }

    @Test
    public void testSample_log() {
        doCodeTest("/parser/sample_log.txt", """
                 Stack:
                   - file: TopClass.py
                     line: 23
                     code: |
                       x = MoreObject("345\\n")
                   - file: MoreClass.py
                     line: 58
                     code: |-
                       foo = bar""");
    }

    @Test
    public void testSeq_seq() {
        doCodeTest("/parser/seq_seq.txt", """
                 - [name        , hr, avg  ]
                 - [Mark McGwire, 65, 0.278]
                 - [Sammy Sosa  , 63, 0.288]""");
    }

    @Test
    public void testSequence_mappings() {
        doCodeTest("/parser/sequence_mappings.txt", """
                 -
                   name: Mark McGwire
                   hr:   65
                   avg:  0.278
                 -
                   name: Sammy Sosa
                   hr:   63
                   avg:  0.288""");
    }

    @Test
    public void testBalance() {
        doCodeTest("/parser/balance.txt", """
                 runningTime: 150000
                 scenarios:
                     voice_bundle_change: {
                         dataCycling: true
                     }
                     smart_overview: {
                         dataCycling: true
                     }""");
    }

    @Test
    public void testInterpolation() {
        doCodeTest("/parser/interpolation.txt", "en:\n  foo: bar %{baz}");
    }

    @Test
    public void testValue_injection() {
        doCodeTest("/parser/value_injection.txt", """
                 key:
                     one: 1 text
                     other: some {{count}} text""");
    }

    @Test
    public void testSequence_idea76804() {
        doCodeTest("/parser/sequence_idea76804.txt", """
                 server:
                 - a
                 - b

                 server:
                   - a
                   - b""");
    }

    @Test
    public void testMultiline_ruby16796() throws Throwable {
        doCodeTest("/parser/multiline_ruby16796.txt", """
                 code:
                   src="keys/{{item}}"
                   mode=0600
                 with_items:
                   - "id_rsa.pub"
                 """);
    }

    @Test
    public void testRuby17389() throws Throwable {
        doCodeTest("/parser/ruby17389.txt", """
                 ---
                 foo: {}
                 bar: "baz\"""");
    }

    @Test
    public void testRuby19105() {
        doCodeTest("/parser/ruby19105.txt", """
                 'Fn::Join':
                   - ''
                   - - Ref: hostedZoneName
                     - a""");
    }

    @Test
    public void testRuby15345() {
        doCodeTest("/parser/ruby15345.txt", """
                 - !qualified.class.name
                     propertyOne: bla bla
                     propertyWithOneSequence:
                         - first value
                     nextPropertyWithOneSequence:
                         - first value of another sequence""");
    }

    @Test
    public void testHonestMultiline() {
        doCodeTest("/parser/honestMultiline.txt", """
                 ---
                 foo: >
                   first text line
                   second text line

                   baz: clazz
                   - this is text
                   - but looks like a list
                   - indent tells.
                 bar: zoo""");
    }

    @Test
    public void testEmptyMultiline() {
        doCodeTest("/parser/emptyMultiline.txt", """
                 ---
                 foo: >
                 bar:
                   abc: def
                   ghi: >
                   jkl: mno
                 baz: qwe""");
    }

    @Test
    public void test001() {
        doTest("/parser/001.concord.yaml");
    }

    protected void doTest(String concordYamlFile) {
        doTest(concordYamlFile, true);
    }

    protected void doCodeTest(String expectedFile, String yaml) {
        doCodeTest(expectedFile, yaml, true);
    }

    protected void doCodeTest(String expectedFile, String yaml, boolean assertNoPsiErrors) {
        var fileName = "a.concord.yaml";
        var file = configureFromText(fileName, yaml);
        doSanityChecks(file);

        var skipSpaces = false;
        var printRanges = false;

        var expected = loadResource(expectedFile, true);
        var actual = toParseTreeText(file, skipSpaces, printRanges).trim();
        Assertions.assertEquals(expected, actual);

        if (assertNoPsiErrors) {
            ParsingTestUtil.assertNoPsiErrorElements(file);
        }
    }

    protected void doTest(String concordYamlFile, boolean assertNoPsiErrors) {
        var fileName = Paths.get(concordYamlFile).getFileName().toString();
        var file = configureFromText(fileName, loadResource(concordYamlFile, true));
        doSanityChecks(file);

        var skipSpaces = false;
        var printRanges = false;

        var expected = loadResource(concordYamlFile.replace(".concord.yaml", ".txt"), true);
        var actual = toParseTreeText(file, skipSpaces, printRanges).trim();
        Assertions.assertEquals(expected, actual);

        if (assertNoPsiErrors) {
            ParsingTestUtil.assertNoPsiErrorElements(file);
        }
        printAstTypeNamesTree(file);
    }

    private void doSanityChecks(PsiFile root) {
        Assertions.assertEquals(root.getViewProvider().getContents().toString(), root.getText(), "psi text mismatch");
        ensureParsed(root);
        ReadAction.run(() -> {
            ensureCorrectReparse(root);
            checkRangeConsistency(root);
        });
    }

    protected static String toParseTreeText(@NotNull PsiElement file,  boolean skipSpaces, boolean printRanges) {
        return DebugUtil.psiToString(file, !skipSpaces, printRanges);
    }

    private void printAstTypeNamesTree(@NotNull PsiFile file) {
        StringBuffer buffer = new StringBuffer();
        Arrays.stream(file.getNode().getChildren(TokenSet.ANY)).forEach(it -> printAstTypeNamesTree(it, buffer, 0));
        System.out.println(buffer);
    }

    private static void printAstTypeNamesTree(ASTNode node, StringBuffer buffer, int indent) {
        buffer.append(" ".repeat(indent));
        buffer.append(node.getElementType()).append("\n");
        indent += 2;
        ASTNode childNode = node.getFirstChildNode();

        while (childNode != null) {
            printAstTypeNamesTree(childNode, buffer, indent);
            childNode = childNode.getTreeNext();
        }
    }

    private static void checkRangeConsistency(PsiFile file) {
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof ForeignLeafPsiElement) return;

                try {
                    ensureNodeRangeConsistency(element, file);
                }
                catch (Throwable e) {
                    throw new AssertionError("In " + element + " of " + element.getClass(), e);
                }
                super.visitElement(element);
            }

            private void ensureNodeRangeConsistency(PsiElement parent, PsiFile file) {
                int parentOffset = parent.getTextRange().getStartOffset();
                int childOffset = 0;
                ASTNode child = parent.getNode().getFirstChildNode();
                if (child != null) {
                    while (child != null) {
                        int childLength = checkChildRangeConsistency(file, parentOffset, childOffset, child);
                        childOffset += childLength;
                        child = child.getTreeNext();
                    }
                    assertEquals(childOffset, parent.getTextLength());
                }
            }

            private static int checkChildRangeConsistency(PsiFile file, int parentOffset, int childOffset, ASTNode child) {
                assertEquals(child.getStartOffsetInParent(), childOffset);
                assertEquals(child.getStartOffset(), childOffset + parentOffset);
                int childLength = child.getTextLength();
                assertEquals(TextRange.from(childOffset + parentOffset, childLength), child.getTextRange());
                if (!(child.getPsi() instanceof ForeignLeafPsiElement)) {
                    assertEquals(child.getTextRange().substring(file.getText()), child.getText());
                }
                return childLength;
            }
        });
    }
}
