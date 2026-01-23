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
}