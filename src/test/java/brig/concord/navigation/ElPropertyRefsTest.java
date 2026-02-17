package brig.concord.navigation;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.el.psi.ElMemberName;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElPropertyRefsTest extends ConcordYamlTestBaseJunit5 {

    private PsiElement resolveElProperty() {
        var offset = myFixture.getCaretOffset();
        var leaf = myFixture.getFile().findElementAt(offset);
        assertNotNull(leaf, "Should find element at caret");

        var memberName = PsiTreeUtil.getParentOfType(leaf, ElMemberName.class, false);
        assertNotNull(memberName, "Should find ElMemberName at caret");

        var refs = memberName.getReferences();
        assertTrue(refs.length > 0, "ElMemberName should have references");

        var resolved = refs[0].resolve();
        assertNotNull(resolved, "Reference should resolve to a declaration");
        return resolved;
    }

    @Test
    void testSetStepPropertyResolves() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        obj:
                          a: 1
                    - log: "${obj.<caret>a}"
                """);

        var resolved = resolveElProperty();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("a", ((YAMLKeyValue) resolved).getKeyText());
    }

    @Test
    void testNestedPropertyResolves() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        obj:
                          inner:
                            x: 1
                    - log: "${obj.inner.<caret>x}"
                """);

        var resolved = resolveElProperty();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("x", ((YAMLKeyValue) resolved).getKeyText());
    }

    @Test
    void testArgumentPropertyResolves() {
        configureFromText("""
                configuration:
                  arguments:
                    config:
                      url: "http://example.com"
                flows:
                  main:
                    - log: "${config.<caret>url}"
                """);

        var resolved = resolveElProperty();
        assertInstanceOf(YAMLKeyValue.class, resolved);
        assertEquals("url", ((YAMLKeyValue) resolved).getKeyText());
    }

    @Test
    void testBuiltInPropertyDoesNotResolve() {
        configureFromText("""
                flows:
                  main:
                    - log: "${initiator.<caret>displayName}"
                """);

        var offset = myFixture.getCaretOffset();
        var leaf = myFixture.getFile().findElementAt(offset);
        assertNotNull(leaf);

        var memberName = PsiTreeUtil.getParentOfType(leaf, ElMemberName.class, false);
        assertNotNull(memberName);

        var refs = memberName.getReferences();
        assertTrue(refs.length > 0);
        assertNull(refs[0].resolve(), "Built-in property should not resolve to PSI element");
    }

    @Test
    void testScalarPropertyDoesNotResolve() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        x: "hello"
                    - log: "${x.<caret>foo}"
                """);

        var offset = myFixture.getCaretOffset();
        var leaf = myFixture.getFile().findElementAt(offset);
        assertNotNull(leaf);

        var memberName = PsiTreeUtil.getParentOfType(leaf, ElMemberName.class, false);
        assertNotNull(memberName);

        var refs = memberName.getReferences();
        assertTrue(refs.length > 0);
        assertNull(refs[0].resolve(), "Property on scalar should not resolve");
    }
}
