// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection.fix;

import brig.concord.inspection.FlowDocumentationInspection;
import brig.concord.inspection.InspectionTestBase;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class ReplaceFlowDocKeywordQuickFixTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(FlowDocumentationInspection.class);
    }

    @Test
    void testFixUnknownKeyword_Mandatory() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
    void testFixUnknownKeyword_Optional() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
    void testFixUnknownKeyword_Fallback() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix 'mandatory' should be available");

        var intentionsOpt = myFixture.filterAvailableIntentions("Change keyword to 'optional'");
        Assertions.assertFalse(intentionsOpt.isEmpty(), "Quick fix 'optional' should be available");

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
