// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection.fix;

import brig.concord.inspection.InspectionTestBase;
import brig.concord.inspection.UnknownKeysInspection;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class AddParameterToFlowDocQuickFixTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(UnknownKeysInspection.class);
    }

    @Test
    void testAddParamToExistingDoc() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
    void testAddParamToNewDoc() {
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
        Assertions.assertTrue(intentions.isEmpty(), "Quick fix should NOT be available");
    }

    @Test
    void testAddParamToMissingDoc() {
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
        Assertions.assertTrue(intentions.isEmpty(), "Quick fix should NOT be available when flow doc is missing");
    }

    @Test
    void testAddParamWithBooleanType() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
    void testAddParamWithIntType() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
    void testAddParamWithObjectType() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
    void testAddParamWithArrayType() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
    void testAddParamWithExpressionType() {
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
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
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
