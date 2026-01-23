package brig.concord.inspection.fix;

import brig.concord.ConcordYamlTestBase;
import brig.concord.inspection.UnknownKeysInspection;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.Test;

public class AddParameterToFlowDocQuickFixTest extends ConcordYamlTestBase {

    @Test
    public void testAddParamToExistingDoc() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"
            
              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    <caret>newParam: "val"
            """);

        var intentions = myFixture.filterAvailableIntentions("Add 'newParam' to flow documentation");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   newParam: string
              ##
              myFlow:
                - log: "Hello"
            
              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    newParam: "val"
            """);
    }

    @Test
    public void testAddParamToNewDoc() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              ##
              # My Flow
              ##
              myFlow:
                - log: "Hello"
            
              caller:
                - call: myFlow
                  in:
                    <caret>newParam: "val"
            """);

        // The inspection does NOT trigger if there are no parameters defined in the doc (it treats it as accepting any params).
        // So we expect NO quick fix here.
        var intentions = myFixture.filterAvailableIntentions("Add 'newParam' to flow documentation");
        assertTrue("Quick fix should NOT be available", intentions.isEmpty());
    }
    
    @Test
    public void testAddParamToMissingDoc() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    <caret>newParam: "val"
            """);

        var intentions = myFixture.filterAvailableIntentions("Add 'newParam' to flow documentation");
        assertTrue("Quick fix should NOT be available when flow doc is missing", intentions.isEmpty());
    }

    @Test
    public void testAddParamWithBooleanType() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    <caret>flag: true
            """);

        var intentions = myFixture.filterAvailableIntentions("Add 'flag' to flow documentation");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   flag: boolean
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    flag: true
            """);
    }

    @Test
    public void testAddParamWithIntType() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    <caret>count: 42
            """);

        var intentions = myFixture.filterAvailableIntentions("Add 'count' to flow documentation");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   count: int
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    count: 42
            """);
    }

    @Test
    public void testAddParamWithObjectType() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    <caret>config:
                      key: value
            """);

        var intentions = myFixture.filterAvailableIntentions("Add 'config' to flow documentation");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   config: object
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    config:
                      key: value
            """);
    }

    @Test
    public void testAddParamWithArrayType() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    <caret>items:
                      - one
                      - two
            """);

        var intentions = myFixture.filterAvailableIntentions("Add 'items' to flow documentation");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   items: object[]
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    items:
                      - one
                      - two
            """);
    }

    @Test
    public void testAddParamWithExpressionType() {
        myFixture.enableInspections(UnknownKeysInspection.class);
        configureFromText("""
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    <caret>dynamic: "${someVar}"
            """);

        var intentions = myFixture.filterAvailableIntentions("Add 'dynamic' to flow documentation");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   dynamic: any
              ##
              myFlow:
                - log: "Hello"

              caller:
                - call: myFlow
                  in:
                    existing: "val"
                    dynamic: "${someVar}"
            """);
    }
}