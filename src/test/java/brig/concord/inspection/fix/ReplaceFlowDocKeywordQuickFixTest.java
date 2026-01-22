package brig.concord.inspection.fix;

import brig.concord.inspection.FlowDocumentationInspection;
import brig.concord.inspection.InspectionTestBase;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

public class ReplaceFlowDocKeywordQuickFixTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(FlowDocumentationInspection.class);
    }

    @Test
    public void testFixUnknownKeyword_Mandatory() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: string, <caret>mandatry
              ##
              myFlow:
                - log: "test"
            """);

        var intentions = myFixture.filterAvailableIntentions("Change keyword to 'mandatory'");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   param: string, mandatory
              ##
              myFlow:
                - log: "test"
            """);
    }

    @Test
    public void testFixUnknownKeyword_Optional() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: string, <caret>optionl
              ##
              myFlow:
                - log: "test"
            """);

        var intentions = myFixture.filterAvailableIntentions("Change keyword to 'optional'");
        assertFalse("Quick fix should be available", intentions.isEmpty());
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   param: string, optional
              ##
              myFlow:
                - log: "test"
            """);
    }

    @Test
    public void testFixUnknownKeyword_Fallback() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: string, <caret>unk
              ##
              myFlow:
                - log: "test"
            """);

        var intentions = myFixture.filterAvailableIntentions("Change keyword to 'mandatory'");
        assertFalse("Quick fix 'mandatory' should be available", intentions.isEmpty());

        var intentionsOpt = myFixture.filterAvailableIntentions("Change keyword to 'optional'");
        assertFalse("Quick fix 'optional' should be available", intentionsOpt.isEmpty());

        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   param: string, mandatory
              ##
              myFlow:
                - log: "test"
            """);
    }
}
