// SPDX-License-Identifier: Apache-2.0
package brig.concord.navigation;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.el.psi.ElIdentifierExpr;
import brig.concord.psi.FlowDocParameter;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ElVariableRefsTest extends ConcordYamlTestBaseJunit5 {

    // ---- forward: expression -> declaration ----

    private PsiElement resolveElVariable() {
        var offset = myFixture.getCaretOffset();
        var leaf = myFixture.getFile().findElementAt(offset);
        assertNotNull(leaf, "Should find element at caret");

        var identExpr = PsiTreeUtil.getParentOfType(leaf, ElIdentifierExpr.class, false);
        assertNotNull(identExpr, "Should find ElIdentifierExpr at caret");

        var refs = identExpr.getReferences();
        assertTrue(refs.length > 0, "ElIdentifierExpr should have references");

        var resolved = refs[0].resolve();
        assertNotNull(resolved, "Reference should resolve to a declaration");
        return resolved;
    }

    // ---- reverse: declaration -> expressions (Find Usages) ----

    private Collection<PsiReference> findUsages(PsiElement declaration) {
        return ReferencesSearch.search(declaration, new LocalSearchScope(myFixture.getFile())).findAll();
    }

    @Test
    void testSetStepVariable() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        myVar: "hello"
                    - log: "${my<caret>Var}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("myVar", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
        var usage = usages.iterator().next().getElement();
        assertInstanceOf(ElIdentifierExpr.class, usage);
    }

    @Test
    void testFlowDocParameter() {
        configureFromText("""
                flows:
                  ##
                  # in:
                  #   bucket: string, mandatory, S3 bucket
                  ##
                  myFlow:
                    - log: "${buck<caret>et}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(FlowDocParameter.class, resolved);
        assertEquals("bucket", ((FlowDocParameter) resolved).getName());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testArgument() {
        configureFromText("""
                configuration:
                  arguments:
                    appName: "test"
                flows:
                  main:
                    - log: "${app<caret>Name}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("appName", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testTaskOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - task: myTask
                      out: result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testTaskOutMapping() {
        configureFromText("""
                flows:
                  main:
                    - task: myTask
                      out:
                        res: ${content}
                    - log: "${r<caret>es}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("res", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testExprOutObject() {
        configureFromText("""
                flows:
                  main:
                    - expr: "${someAction()}"
                      out:
                        res: ${content}
                    - log: "${r<caret>es}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("res", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testExprOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - expr: "${someAction()}"
                      out: result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testTryOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - try:
                        - log: "hello"
                      out: result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testBlockOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - block:
                        - log: "hello"
                      out: result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testBlockOutArray() {
        configureFromText("""
                flows:
                  main:
                    - block:
                        - log: "hello"
                      out:
                        - result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testTryOutArray() {
        configureFromText("""
                flows:
                  main:
                    - try:
                        - log: "hello"
                      out:
                        - result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testParallelOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - parallel:
                        - log: "hello"
                      out: result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testParallelOutObject() {
        configureFromText("""
                flows:
                  main:
                    - parallel:
                        - log: "hello"
                      out:
                        res: ${content}
                    - log: "${r<caret>es}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("res", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testParallelOutArray() {
        configureFromText("""
                flows:
                  main:
                    - parallel:
                        - log: "hello"
                      out:
                        - result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testScriptOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - script: groovy
                      body: "x = 1"
                      out: result
                    - log: "${resu<caret>lt}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testScriptOutObject() {
        configureFromText("""
                flows:
                  main:
                    - script: groovy
                      body: "x = 1"
                      out:
                        res: ${content}
                    - log: "${r<caret>es}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("res", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testCallOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out: result
                    - log: "${resu<caret>lt}"
                  helper:
                    - log: "hi"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testCallOutObject() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out:
                        res: ${content}
                    - log: "${r<caret>es}"
                  helper:
                    - log: "hi"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("res", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testCallOutArray() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out:
                        - result
                    - log: "${resu<caret>lt}"
                  helper:
                    - log: "hi"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLScalar.class, resolved);
        assertEquals("result", ((YAMLScalar) resolved).getTextValue());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testPlainTextExpression() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        x: 1
                    - if: ${<caret>x}
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("x", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(1, usages.size());
    }

    @Test
    void testMultipleUsages() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        v: 1
                    - log: "${<caret>v}"
                    - if: ${v > 0}
                      then:
                        - log: "${v}"
                """);

        var resolved = resolveElVariable();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("v", ((YAMLKeyValue) resolved).getKeyText());

        var usages = findUsages(resolved);
        assertEquals(3, usages.size());
        for (var ref : usages) {
            assertInstanceOf(ElIdentifierExpr.class, ref.getElement());
        }
    }

    @Test
    void testAllOutVariantsResolution() {
        configureFromText("""
                configuration:
                  arguments:
                    argVar: "from args"
                flows:
                  ##
                  # in:
                  #   docParam: string, mandatory, input
                  ##
                  main:
                    - set:
                        setVar: 1
                    - task: myTask
                      out: taskScalar
                    - task: myTask
                      out:
                        taskObj: ${content}
                    - expr: "${action()}"
                      out: exprScalar
                    - expr: "${action()}"
                      out:
                        exprObj: ${content}
                    - try:
                        - log: "hello"
                      out: tryScalar
                    - try:
                        - log: "hello"
                      out:
                        - tryItem
                    - block:
                        - log: "hello"
                      out: blockScalar
                    - block:
                        - log: "hello"
                      out:
                        - blockItem
                    - parallel:
                        - log: "hello"
                      out: parallelScalar
                    - parallel:
                        - log: "hello"
                      out:
                        parallelObj: ${content}
                    - parallel:
                        - log: "hello"
                      out:
                        - parallelItem
                    - script: groovy
                      body: "x = 1"
                      out: scriptScalar
                    - script: groovy
                      body: "x = 1"
                      out:
                        scriptObj: ${content}
                    - call: helper
                      out: callScalar
                    - call: helper
                      out:
                        callObj: ${content}
                    - call: helper
                      out:
                        - callItem
                    - log: "${argVar} ${docParam} ${setVar}"
                    - log: "${taskScalar} ${taskObj} ${exprScalar} ${exprObj}"
                    - log: "${tryScalar} ${tryItem} ${blockScalar} ${blockItem}"
                    - log: "${parallelScalar} ${parallelObj} ${parallelItem}"
                    - log: "${scriptScalar} ${scriptObj}"
                    - log: "${callScalar} ${callObj} ${callItem}"
                  helper:
                    - log: "hi"
                """);

        // variable name -> expected declaration PSI type
        var expected = Map.ofEntries(
                Map.entry("argVar", YAMLKeyValue.class),
                Map.entry("docParam", FlowDocParameter.class),
                Map.entry("setVar", YAMLKeyValue.class),
                // task out
                Map.entry("taskScalar", YAMLScalar.class),
                Map.entry("taskObj", YAMLKeyValue.class),
                // expr out
                Map.entry("exprScalar", YAMLScalar.class),
                Map.entry("exprObj", YAMLKeyValue.class),
                // try out
                Map.entry("tryScalar", YAMLScalar.class),
                Map.entry("tryItem", YAMLScalar.class),
                // block out
                Map.entry("blockScalar", YAMLScalar.class),
                Map.entry("blockItem", YAMLScalar.class),
                // parallel out
                Map.entry("parallelScalar", YAMLScalar.class),
                Map.entry("parallelObj", YAMLKeyValue.class),
                Map.entry("parallelItem", YAMLScalar.class),
                // script out
                Map.entry("scriptScalar", YAMLScalar.class),
                Map.entry("scriptObj", YAMLKeyValue.class),
                // call out
                Map.entry("callScalar", YAMLScalar.class),
                Map.entry("callObj", YAMLKeyValue.class),
                Map.entry("callItem", YAMLScalar.class)
        );

        var allIdents = PsiTreeUtil.findChildrenOfType(myFixture.getFile(), ElIdentifierExpr.class);
        var resolved = new LinkedHashSet<String>();

        for (var ident : allIdents) {
            var name = ident.getText();
            var expectedClass = expected.get(name);
            if (expectedClass == null) {
                continue; // skip "content", "action", etc.
            }

            var refs = ident.getReferences();
            assertTrue(refs.length > 0, "'" + name + "' should have references");

            var target = refs[0].resolve();
            assertNotNull(target, "'" + name + "' should resolve");
            assertInstanceOf(expectedClass, target, "Wrong declaration type for '" + name + "'");
            resolved.add(name);
        }

        assertEquals(expected.size(), resolved.size(),
                "All expected variables should resolve. Missing: " + difference(expected.keySet(), resolved));
    }

    private static <T> java.util.Set<T> difference(java.util.Set<T> a, java.util.Set<T> b) {
        var diff = new LinkedHashSet<>(a);
        diff.removeAll(b);
        return diff;
    }

    @Test
    void testBuiltInNoDeclaration() {
        configureFromText("""
                flows:
                  main:
                    - log: "${tx<caret>Id}"
                """);

        var offset = myFixture.getCaretOffset();
        var leaf = myFixture.getFile().findElementAt(offset);
        var identExpr = PsiTreeUtil.getParentOfType(leaf, ElIdentifierExpr.class, false);
        assertNotNull(identExpr);

        var refs = identExpr.getReferences();
        assertTrue(refs.length > 0, "Should still have a reference");
        assertNull(refs[0].resolve(), "Built-in variable reference should not resolve");
    }
}
