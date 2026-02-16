package brig.concord.el;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.junit5.RunInEdt;
import com.intellij.testFramework.junit5.impl.TestApplicationExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestApplicationExtension.class)
@RunInEdt(writeIntent = true)
class ElParserTest extends TestBaseJunit5 {

    // ---- Valid expressions: parse without errors ----

    @ParameterizedTest(name = "valid: {0}")
    @ValueSource(strings = {
            // Identifiers
            "myVar",
            "x",
            "_private",
            "$dollar",

            // Literals
            "42",
            "3.14",
            "1e5",
            ".5",
            "'hello'",
            "\"hello\"",
            "'it\\'s'",
            "true",
            "false",
            "null",

            // Property access
            "a.b",
            "a.b.c.d",

            // Array/map access
            "a[0]",
            "a['key']",
            "a[b.c]",

            // Method calls
            "a.b()",
            "a.b(1, 2)",
            "a.b().c()",
            "obj.method(a, b, c)",

            // Top-level function calls
            "hasVariable('x')",
            "uuid()",
            "orDefault('var', 'default')",

            // Arithmetic
            "a + b",
            "a - b",
            "a * b",
            "a / b",
            "a % b",
            "a div b",
            "a mod b",
            "a + b * c - d",

            // Comparison
            "a < b",
            "a > b",
            "a <= b",
            "a >= b",
            "a lt b",
            "a gt b",
            "a le b",
            "a ge b",

            // Equality
            "a == b",
            "a != b",
            "a eq b",
            "a ne b",

            // Logical
            "a && b",
            "a || b",
            "a and b",
            "a or b",
            "!a",
            "not a",

            // Empty
            "empty list",
            "empty a.b",

            // Ternary
            "a ? b : c",
            "a > 0 ? 'positive' : 'negative'",
            "a ? b ? c : d : e",

            // String concatenation
            "a += b",
            "'hello' += ' ' += 'world'",

            // Lambda (single param)
            "x -> x + 1",

            // Lambda (multi param via paren)
            "(x, y) -> x + y",

            // Immediately invoked lambda
            "((x) -> x + 1)(5)",

            // Assignment
            "x = 5",

            // Semicolons
            "a; b; c",
            "x = 1; y = 2",

            // Collection literals
            "[1, 2, 3]",
            "[]",
            "{'key': 'value'}",
            "{'a': 1, 'b': 2}",
            "{}",
            "{x}",

            // Complex
            "a.b.c(1, 'x')[0].d",
            "empty list || list.size() == 0",
            "a > b && c < d || e eq f",
            "items.stream().filter(x -> x > 5).toList()",

            // Nested method calls
            "a(b(c(1)))",

            // Mixed access
            "a[0].b('x')[1]",

            // Keywords as property names
            "obj.class",
            "obj.empty",
            "obj.true",
            "obj.not",

            // instanceof
            "a instanceof 'java.lang.String'",

            // Unary chain
            "- - a",
            "!!a",
            "not not a",
    })
    void validExpression(String expr) {
        var file = parseEl(expr);
        var errors = collectErrors(file);
        assertThat(errors)
                .as("Expected no parse errors for: %s\nPSI:\n%s", expr, psiToString(file))
                .isEmpty();
    }

    // ---- Specific PSI structure tests ----

    @Test
    void propertyAccessStructure() {
        var file = parseEl("a.b.c");
        assertPsiStructure(file, """
                EL Expression File
                  ElAccessExprImpl(ACCESS_EXPR)
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('a')
                    ElDotSuffixImpl(DOT_SUFFIX)
                      PsiElement(ElTokenType..)('.')
                      ElMemberNameImpl(MEMBER_NAME)
                        PsiElement(ElTokenType.IDENTIFIER)('b')
                    ElDotSuffixImpl(DOT_SUFFIX)
                      PsiElement(ElTokenType..)('.')
                      ElMemberNameImpl(MEMBER_NAME)
                        PsiElement(ElTokenType.IDENTIFIER)('c')
                """);
    }

    @Test
    void methodCallStructure() {
        var file = parseEl("a.method(1, 'x')");
        assertPsiStructure(file, """
                EL Expression File
                  ElAccessExprImpl(ACCESS_EXPR)
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('a')
                    ElDotSuffixImpl(DOT_SUFFIX)
                      PsiElement(ElTokenType..)('.')
                      ElMemberNameImpl(MEMBER_NAME)
                        PsiElement(ElTokenType.IDENTIFIER)('method')
                      ElArgListImpl(ARG_LIST)
                        PsiElement(ElTokenType.()('(')
                        ElLiteralImpl(LITERAL)
                          PsiElement(ElTokenType.INTEGER_LITERAL)('1')
                        PsiElement(ElTokenType.,)(',')
                        ElStringLiteralImpl(STRING_LITERAL)
                          PsiElement(ElTokenType.SINGLE_QUOTED_STRING)(''x'')
                        PsiElement(ElTokenType.))(')')
                """);
    }

    @Test
    void ternaryStructure() {
        var file = parseEl("a ? b : c");
        assertPsiStructure(file, """
                EL Expression File
                  ElChoiceExprImpl(CHOICE_EXPR)
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('a')
                    PsiElement(ElTokenType.?)('?')
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('b')
                    PsiElement(ElTokenType.:)(':')
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('c')
                """);
    }

    @Test
    void binaryOperatorStructure() {
        var file = parseEl("a + b * c");
        assertPsiStructure(file, """
                EL Expression File
                  ElAddExprImpl(ADD_EXPR)
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('a')
                    PsiElement(ElTokenType.+)('+')
                    ElMulExprImpl(MUL_EXPR)
                      ElIdentifierExprImpl(IDENTIFIER_EXPR)
                        PsiElement(ElTokenType.IDENTIFIER)('b')
                      PsiElement(ElTokenType.*)('*')
                      ElIdentifierExprImpl(IDENTIFIER_EXPR)
                        PsiElement(ElTokenType.IDENTIFIER)('c')
                """);
    }

    @Test
    void unaryStructure() {
        var file = parseEl("!empty list");
        assertPsiStructure(file, """
                EL Expression File
                  ElPrefixExprImpl(PREFIX_EXPR)
                    PsiElement(ElTokenType.!)('!')
                    ElPrefixExprImpl(PREFIX_EXPR)
                      PsiElement(ElTokenType.empty)('empty')
                      ElIdentifierExprImpl(IDENTIFIER_EXPR)
                        PsiElement(ElTokenType.IDENTIFIER)('list')
                """);
    }

    @Test
    void lambdaStructure() {
        var file = parseEl("x -> x + 1");
        assertPsiStructure(file, """
                EL Expression File
                  ElAssignExprImpl(ASSIGN_EXPR)
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('x')
                    PsiElement(ElTokenType.->)('->')
                    ElAddExprImpl(ADD_EXPR)
                      ElIdentifierExprImpl(IDENTIFIER_EXPR)
                        PsiElement(ElTokenType.IDENTIFIER)('x')
                      PsiElement(ElTokenType.+)('+')
                      ElLiteralImpl(LITERAL)
                        PsiElement(ElTokenType.INTEGER_LITERAL)('1')
                """);
    }

    @Test
    void collectionLiteralsStructure() {
        var file = parseEl("[1, 2, 3]");
        assertPsiStructure(file, """
                EL Expression File
                  ElListLiteralImpl(LIST_LITERAL)
                    PsiElement(ElTokenType.[)('[')
                    ElLiteralImpl(LITERAL)
                      PsiElement(ElTokenType.INTEGER_LITERAL)('1')
                    PsiElement(ElTokenType.,)(',')
                    ElLiteralImpl(LITERAL)
                      PsiElement(ElTokenType.INTEGER_LITERAL)('2')
                    PsiElement(ElTokenType.,)(',')
                    ElLiteralImpl(LITERAL)
                      PsiElement(ElTokenType.INTEGER_LITERAL)('3')
                    PsiElement(ElTokenType.])(']')
                """);

        file = parseEl("{'a': 1}");
        assertPsiStructure(file, """
                EL Expression File
                  ElMapLiteralImpl(MAP_LITERAL)
                    PsiElement(ElTokenType.{)('{')
                    ElMapEntryImpl(MAP_ENTRY)
                      ElStringLiteralImpl(STRING_LITERAL)
                        PsiElement(ElTokenType.SINGLE_QUOTED_STRING)(''a'')
                      PsiElement(ElTokenType.:)(':')
                      ElLiteralImpl(LITERAL)
                        PsiElement(ElTokenType.INTEGER_LITERAL)('1')
                    PsiElement(ElTokenType.})('}')
                """);
    }

    @Test
    void semicolonSequence() {
        var file = parseEl("a = 1; b = 2; c");
        assertPsiStructure(file, """
                EL Expression File
                  ElExpressionImpl(EXPRESSION)
                    ElAssignExprImpl(ASSIGN_EXPR)
                      ElIdentifierExprImpl(IDENTIFIER_EXPR)
                        PsiElement(ElTokenType.IDENTIFIER)('a')
                      PsiElement(ElTokenType.=)('=')
                      ElLiteralImpl(LITERAL)
                        PsiElement(ElTokenType.INTEGER_LITERAL)('1')
                    PsiElement(ElTokenType.;)(';')
                    ElAssignExprImpl(ASSIGN_EXPR)
                      ElIdentifierExprImpl(IDENTIFIER_EXPR)
                        PsiElement(ElTokenType.IDENTIFIER)('b')
                      PsiElement(ElTokenType.=)('=')
                      ElLiteralImpl(LITERAL)
                        PsiElement(ElTokenType.INTEGER_LITERAL)('2')
                    PsiElement(ElTokenType.;)(';')
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('c')
                """);
    }

    @Test
    void bracketAccessStructure() {
        var file = parseEl("map['key']");
        assertPsiStructure(file, """
                EL Expression File
                  ElAccessExprImpl(ACCESS_EXPR)
                    ElIdentifierExprImpl(IDENTIFIER_EXPR)
                      PsiElement(ElTokenType.IDENTIFIER)('map')
                    ElBracketSuffixImpl(BRACKET_SUFFIX)
                      PsiElement(ElTokenType.[)('[')
                      ElStringLiteralImpl(STRING_LITERAL)
                        PsiElement(ElTokenType.SINGLE_QUOTED_STRING)(''key'')
                      PsiElement(ElTokenType.])(']')
                """);
    }

    // ---- Collapse verification tests ----

    @Test
    void simpleIdentifierCollapsed() {
        var file = parseEl("myVar");
        assertPsiStructure(file, """
                EL Expression File
                  ElIdentifierExprImpl(IDENTIFIER_EXPR)
                    PsiElement(ElTokenType.IDENTIFIER)('myVar')
                """);
    }

    @Test
    void simpleLiteralCollapsed() {
        var file = parseEl("42");
        assertPsiStructure(file, """
                EL Expression File
                  ElLiteralImpl(LITERAL)
                    PsiElement(ElTokenType.INTEGER_LITERAL)('42')
                """);
    }

    @Test
    void stringLiteralCollapsed() {
        var file = parseEl("'hello'");
        assertPsiStructure(file, """
                EL Expression File
                  ElStringLiteralImpl(STRING_LITERAL)
                    PsiElement(ElTokenType.SINGLE_QUOTED_STRING)(''hello'')
                """);
    }

    @Test
    void emptyExpressionIsValid() {
        var file = parseEl("");
        var errors = collectErrors(file);
        assertThat(errors).isEmpty();
    }

    @Test
    void whitespaceOnlyIsValid() {
        var file = parseEl("   ");
        var errors = collectErrors(file);
        assertThat(errors).isEmpty();
    }

    // ---- Helpers ----

    private PsiFile parseEl(String text) {
        return myFixture.configureByText("test.el", text);
    }

    private static void assertPsiStructure(PsiFile file, String expected) {
        assertNoPsiErrors(file);
        assertThat(psiStructure(file)).isEqualTo(expected.stripTrailing());
    }

    private static void assertNoPsiErrors(PsiFile file) {
        var errors = collectErrors(file);
        assertThat(errors)
                .as("PSI:\n%s", psiToString(file))
                .isEmpty();
    }

    private static List<String> collectErrors(PsiFile file) {
        var errors = new ArrayList<String>();
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof PsiErrorElement err) {
                    errors.add(err.getErrorDescription());
                }
                super.visitElement(element);
            }
        });
        return errors;
    }

    private static String psiStructure(PsiFile file) {
        return DebugUtil.psiToString(file, false, false).stripTrailing();
    }

    private static String psiToString(PsiFile file) {
        return DebugUtil.psiToString(file, true, false);
    }
}
