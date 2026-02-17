// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import brig.concord.ConcordBundle;
import brig.concord.inspection.InspectionTestBase;
import brig.concord.inspection.MissingKeysInspection;
import brig.concord.inspection.UnknownKeysInspection;
import brig.concord.inspection.ValueInspection;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class TaskSchemaInspectionTest extends InspectionTestBase {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        // register a provider that also loads test-only schemas
        var registry = TaskSchemaRegistry.getInstance(getProject());
        registry.setProvider(taskName -> {
            var path = "/taskSchema/" + taskName + ".schema.json";
            return TaskSchemaInspectionTest.class.getResourceAsStream(path);
        });
    }

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(UnknownKeysInspection.class, MissingKeysInspection.class, ValueInspection.class);
    }

    // --- positive tests: valid YAML, no errors ---

    @Test
    void testValidConcordTask_noErrors() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: start
                        project: myProject
                      out:
                        ok: true
                """);

        assertNoErrors();
    }

    @Test
    void testAdditionalPropertiesAllowed_noErrors() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: start
                        project: myProject
                        customParam: someValue
                """);

        assertNoErrors();
    }

    @Test
    void testValidStrictTask_noErrors() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      in:
                        url: "http://example.com"
                        method: GET
                """);

        assertNoErrors();
    }

    @Test
    void testUnknownTask_noErrors() {
        configureFromText("""
                flows:
                  main:
                    - task: unknownTask
                      in:
                        anything: goes
                """);

        assertNoErrors();
    }

    // --- negative tests: unknown keys ---

    @Test
    void testUnknownKeyWithStrictSchema() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      in:
                        url: "http://example.com"
                        unknownParam: value
                """);

        inspection(key("/flows/main[0]/in/unknownParam"))
                .expectHighlight(ConcordBundle.message(
                        "YamlUnknownKeysInspectionBase.unknown.key", "unknownParam"));
    }

    @Test
    void testMultipleUnknownKeysWithStrictSchema() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      in:
                        url: "http://example.com"
                        foo: bar
                        baz: qux
                """);

        inspection(key("/flows/main[0]/in/foo"))
                .expectHighlight(ConcordBundle.message(
                        "YamlUnknownKeysInspectionBase.unknown.key", "foo"));
        inspection(key("/flows/main[0]/in/baz"))
                .expectHighlight(ConcordBundle.message(
                        "YamlUnknownKeysInspectionBase.unknown.key", "baz"));
    }

    // --- negative tests: missing required keys ---

    @Test
    void testMissingRequiredKey() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      in:
                        method: GET
                """);

        inspection(key("/flows/main[0]/in"))
                .expectHighlight(ConcordBundle.message(
                        "YamlMissingKeysInspectionBase.missing.keys", "url"));
    }

    // --- negative tests: invalid enum values ---

    @Test
    void testInvalidEnumValue() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      in:
                        url: "http://example.com"
                        method: PATCH
                """);

        inspection(value("/flows/main[0]/in/method"))
                .expectHighlight(ConcordBundle.message(
                        "invalid.value", "string|expression"));
    }

    @Test
    void testInvalidConcordActionEnum() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: invalidAction
                """);

        inspection(value("/flows/main[0]/in/action"))
                .expectHighlight(ConcordBundle.message(
                        "invalid.value", "string|expression"));
    }

    // --- oneOf polymorphic types ---

    @Test
    void testOneOfPolymorphicType_arrayValueShouldBeValid() {
        // concord schema defines instanceId as oneOf: [string, array<string>]
        // and the parser now reads oneOf into a composite schema type,
        // so both a single string and an array of strings should validate without errors
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: kill
                        instanceId:
                          - "id-1"
                          - "id-2"
                """);

        assertNoErrors();
    }

    // --- nested object tests ---

    @Test
    void testValidNestedObject_noErrors() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        auth:
                          basic:
                            username: admin
                            password: secret
                """);

        assertNoErrors();
    }

    @Test
    void testUnknownKeyInNestedObject() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        auth:
                          unknownField: value
                """);

        inspection(key("/flows/main[0]/in/auth/unknownField"))
                .expectHighlight(ConcordBundle.message(
                        "YamlUnknownKeysInspectionBase.unknown.key", "unknownField"));
    }

    @Test
    void testUnknownKeyInDeepNestedObject() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        auth:
                          basic:
                            username: admin
                            password: secret
                            extra: value
                """);

        inspection(key("/flows/main[0]/in/auth/basic/extra"))
                .expectHighlight(ConcordBundle.message(
                        "YamlUnknownKeysInspectionBase.unknown.key", "extra"));
    }

    @Test
    void testMissingRequiredKeyInNestedObject() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        auth:
                          basic:
                            username: admin
                """);

        inspection(key("/flows/main[0]/in/auth/basic"))
                .expectHighlight(ConcordBundle.message(
                        "YamlMissingKeysInspectionBase.missing.keys", "password"));
    }

    @Test
    void testExpressionInPlaceOfNestedObject_noErrors() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        auth: ${authConfig}
                """);

        assertNoErrors();
    }

    @Test
    void testFreeFormObjectAcceptsAnyKeys_noErrors() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        freeFormObject:
                          anything: goes
                          nested:
                            also: works
                """);

        assertNoErrors();
    }
}
