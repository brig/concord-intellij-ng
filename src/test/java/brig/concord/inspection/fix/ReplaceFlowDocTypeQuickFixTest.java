package brig.concord.inspection.fix;

import brig.concord.inspection.FlowDocumentationInspection;
import brig.concord.inspection.InspectionTestBase;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class ReplaceFlowDocTypeQuickFixTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(FlowDocumentationInspection.class);
    }

    @Test
    void testFixUnknownType_String() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: <caret>str, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        var intentions = myFixture.filterAvailableIntentions("Change type to 'string'");
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
    void testCaseInsensitiveType_noWarning() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: <caret>String, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        // "String" is valid (case-insensitive match), so no quick fix should be offered
        var intentions = myFixture.filterAvailableIntentions("Change type to 'string'");
        Assertions.assertTrue(intentions.isEmpty(), "No quick fix expected for valid case-insensitive type");
    }

    @Test
    void testFixUnknownType_Array() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: <caret>string_list, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        // "list" triggers array suggestions
        var intentions = myFixture.filterAvailableIntentions("Change type to 'string[]'");
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   param: string[], mandatory
              ##
              myFlow:
                - log: "test"
            """);
    }
    @Test
    void testFixUnknownType_ArrayPreservesArraySuffix() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: <caret>strng[], mandatory
              ##
              myFlow:
                - log: "test"
            """);

        // "strng[]" should suggest "string[]", preserving the array suffix
        var intentions = myFixture.filterAvailableIntentions("Change type to 'string[]'");
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should suggest 'string[]' for 'strng[]'");
        myFixture.launchAction(intentions.getFirst());

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   param: string[], mandatory
              ##
              myFlow:
                - log: "test"
            """);
    }

    @Test
    void testFixUnknownType_Fallback() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: <caret>unknown, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        // "unknown" triggers fallback suggestions, including 'string'
        var intentions = myFixture.filterAvailableIntentions("Change type to 'string'");
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
}
