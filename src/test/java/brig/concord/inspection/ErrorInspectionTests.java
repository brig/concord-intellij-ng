// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.assertions.InspectionAssertions;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static brig.concord.meta.model.value.ParamMetaTypes.*;

class ErrorInspectionTests extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(MissingKeysInspection.class, UnknownKeysInspection.class, ValueInspection.class);
    }

    private void runErrorTest(String category, String file, Consumer<InspectionAssertions> assertions) {
        configureFromResource("/errors/" + category + "/" + file + ".concord.yml");
        var i = inspection();
        assertions.accept(i);
        i.check();
    }

    private static Arguments args(String file, Consumer<InspectionAssertions> assertions) {
        return Arguments.of(file, assertions);
    }

    // --- checkpoint ---

    static Stream<Arguments> checkpointCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertStringValueExpected),
                args("002", a -> a.assertUnknownKey("trash")),
                args("003", InspectionAssertions::assertObjectRequired),
                args("004", InspectionAssertions::assertValueRequired),
                args("005", a -> a.assertUnexpectedKey("trash"))
        );
    }

    @ParameterizedTest(name = "checkpoint/{0}")
    @MethodSource("checkpointCases")
    void testCheckpoint(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("checkpoint", file, assertions);
    }

    // --- expression ---

    static Stream<Arguments> expressionCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", a -> a.assertHasError(ConcordBundle.message("ExpressionType.error.invalid.value"))),
                args("002", InspectionAssertions::assertValueRequired),
                args("003", InspectionAssertions::assertStringValueExpected),
                args("005", InspectionAssertions::assertValueRequired),
                args("006", InspectionAssertions::assertObjectRequired),
                args("007", InspectionAssertions::assertValueRequired),
                args("008", InspectionAssertions::assertArrayRequired),
                // TODO: IdentityElementMetaType.validateValue
                args("009", InspectionAssertions::assertArrayRequired),
                args("010", InspectionAssertions::assertUnknownStep)
        );
    }

    @ParameterizedTest(name = "expression/{0}")
    @MethodSource("expressionCases")
    void testExpression(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("expression", file, assertions);
    }

    // --- imports ---

    static Stream<Arguments> importsCases() {
        return Stream.of(
                args("001", InspectionAssertions::assertArrayRequired),
                args("002", a -> a.assertArrayRequired().assertUnexpectedKey("k")),
                args("002_1", a -> a.assertUnexpectedKey("k")),
                args("003", InspectionAssertions::assertValueRequired),
                args("004", InspectionAssertions::assertObjectRequired),
                args("005", a -> a.assertValueRequired().assertUnknownKey("trash")),
                args("006", InspectionAssertions::assertStringValueExpected),
                args("007", InspectionAssertions::assertStringValueExpected),
                args("008", InspectionAssertions::assertValueRequired),
                args("009", InspectionAssertions::assertStringValueExpected),
                args("010", a -> a.assertMissingKey("name")),
                args("011", a -> a.assertUnexpectedKey("git-trash")),
                args("012", InspectionAssertions::assertArrayRequired),
                args("013", a -> a.assertUnexpectedKey("trash")),
                args("014", a -> a.assertObjectRequired().assertObjectRequired()),
                args("015", InspectionAssertions::assertStringValueExpected),
                args("016", a -> a.assertHasError("Valid regular expression or string required. Error: 'Unclosed character class near index 1\n" +
                        "[.\n" +
                        " ^'"))
        );
    }

    @ParameterizedTest(name = "imports/{0}")
    @MethodSource("importsCases")
    void testImports(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("imports", file, assertions);
    }

    // --- triggers ---

    static Stream<Arguments> triggersCases() {
        return Stream.of(
                args("001", InspectionAssertions::assertArrayRequired),
                args("002", InspectionAssertions::assertValueRequired),
                args("003", a -> a.assertMissingKey("conditions, entryPoint").assertIntExpected()),
                args("004", a -> a.assertMissingKey("conditions, entryPoint")),
                args("005", a -> a.assertStringValueExpected().assertUndefinedFlow()),
                args("006", a -> a.assertMissingKey("conditions")),
                args("007", InspectionAssertions::assertObjectRequired),
                args("008", a -> a.assertMissingKey("type").assertUnexpectedKey("test")),
                args("009", InspectionAssertions::assertArrayRequired),
                args("010", InspectionAssertions::assertBooleanExpected),
                args("011", InspectionAssertions::assertBooleanExpected),
                args("012", InspectionAssertions::assertObjectRequired),
                args("013", InspectionAssertions::assertObjectRequired),
                args("014", a -> a.assertHasError("Valid regular expression or string required. Error: 'Dangling meta character '*' near index 0\n" +
                        "*\n" +
                        "^'")),
                args("015", a -> a.assertMissingKey("conditions, entryPoint")),
                args("016", InspectionAssertions::assertValueRequired),
                args("017", a -> a.assertMissingKey("entryPoint").assertStringValueExpected()),
                args("018", a -> a.assertStringValueExpected().assertStringValueExpected().assertUndefinedFlow()),
                args("019", a -> a.assertStringValueExpected().assertUndefinedFlow()),
                args("020", InspectionAssertions::assertArrayRequired),
                args("021", InspectionAssertions::assertObjectRequired),
                args("022", InspectionAssertions::assertValueRequired),
                args("023", a -> a.assertMissingKey("entryPoint")),
                args("024", InspectionAssertions::assertArrayRequired),
                // @Disabled("oneops") - 025, 026, 027
                // args("025", InspectionAssertions::assertArrayRequired),
                // args("026", InspectionAssertions::assertArrayRequired),
                // args("027", InspectionAssertions::assertArrayRequired),
                // @Disabled("custom trigger")
                // args("028", InspectionAssertions::assertArrayRequired),
                args("029", InspectionAssertions::assertSingleValueExpected),
                args("030", a -> a.assertHasError("Valid timezone required")),
                args("031", a -> a.assertUnknownKey("trash")),
                args("031_1", a -> a.assertUnknownKey("unknown")),
                args("032", InspectionAssertions::assertObjectRequired),
                args("033", InspectionAssertions::assertValueRequired),
                args("034", a -> a.assertUnknownKey("trash")),
                args("035", InspectionAssertions::assertBooleanExpected),
                args("036", InspectionAssertions::assertStringValueExpected),
                args("037", a -> a.assertMissingKey("group or groupBy"))
        );
    }

    @ParameterizedTest(name = "triggers/{0}")
    @MethodSource("triggersCases")
    void testTriggers(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("triggers", file, assertions);
    }

    // --- tasks ---

    static Stream<Arguments> tasksCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertStringValueExpected),
                args("002", InspectionAssertions::assertValueRequired),
                args("003", InspectionAssertions::assertStringValueExpected),
                args("005", InspectionAssertions::assertValueRequired),
                args("006", InspectionAssertions::assertExpressionExpected),
                args("007", InspectionAssertions::assertValueRequired),
                args("008", InspectionAssertions::assertValueRequired),
                args("009", InspectionAssertions::assertObjectRequired),
                args("009_1", InspectionAssertions::assertValueRequired),
                args("009_2", InspectionAssertions::assertValueRequired),
                args("009_3", a -> a.assertInvalidValue(NUMBER_OR_EXPRESSION)),
                args("009_4", a -> a.assertUnexpectedValue("a")),
                args("010", InspectionAssertions::assertObjectRequired),
                args("011", InspectionAssertions::assertValueRequired),
                args("012", InspectionAssertions::assertValueRequired),
                args("013", InspectionAssertions::assertValueRequired),
                args("014", InspectionAssertions::assertObjectRequired),
                args("015", a -> a.assertUnexpectedKey("trash")),
                args("016", InspectionAssertions::assertValueRequired),
                args("017", InspectionAssertions::assertObjectRequired),
                args("018", InspectionAssertions::assertExpressionExpected),
                args("019", InspectionAssertions::assertStringValueExpected),
                args("020", a -> a.assertUnexpectedValue("trash").assertExpressionExpected()),
                args("021", a -> a.assertInvalidValue(NUMBER_OR_EXPRESSION))
        );
    }

    @ParameterizedTest(name = "tasks/{0}")
    @MethodSource("tasksCases")
    void testTasks(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("tasks", file, assertions);
    }

    // --- flowCall ---

    static Stream<Arguments> flowCallCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", a -> a.assertStringValueExpected().assertUndefinedFlow()),
                args("002", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("003", a -> a.assertStringValueExpected().assertUndefinedFlow()),
                args("005", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("006", a -> a.assertExpressionExpected().assertUndefinedFlow()),
                args("007", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("008", a -> a.assertUnknownKey("withItems").assertUndefinedFlow()),
                args("009", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("010", a -> a.assertObjectRequired().assertUndefinedFlow()),
                args("011", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("012", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("013", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("014", a -> a.assertObjectRequired().assertUndefinedFlow()),
                args("015", a -> a.assertUnknownKey("trash").assertUndefinedFlow()),
                args("016", a -> a.assertValueRequired().assertUndefinedFlow()),
                args("017", a -> a.assertObjectRequired().assertUndefinedFlow()),
                args("018", a -> a.assertStringValueExpected().assertUndefinedFlow()),
                args("019", a -> a.assertObjectRequired().assertUndefinedFlow()),
                args("020", InspectionAssertions::assertUndefinedFlow),
                args("021", a -> a.assertUndefinedFlow().assertExpressionExpected()),
                args("022", a -> a.assertUnexpectedKey("unknown").assertMissingKey("p1")),
                args("023", a -> a.assertInvalidValue(STRING_OR_EXPRESSION))
        );
    }

    @ParameterizedTest(name = "flowCall/{0}")
    @MethodSource("flowCallCases")
    void testFlowCall(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("flowCall", file, assertions);
    }

    // --- group ---

    static Stream<Arguments> groupCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertArrayRequired),
                args("002", InspectionAssertions::assertArrayRequired),
                args("003", a -> a.assertUnknownKey("trash")),
                args("004", InspectionAssertions::assertValueRequired),
                args("005", InspectionAssertions::assertValueRequired),
                args("006", InspectionAssertions::assertValueRequired),
                args("007", a -> a.assertUnknownKey("trash")),
                args("008", InspectionAssertions::assertValueRequired)
        );
    }

    @ParameterizedTest(name = "group/{0}")
    @MethodSource("groupCases")
    void testGroup(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("group", file, assertions);
    }

    // --- parallel ---

    static Stream<Arguments> parallelCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertArrayRequired),
                args("002", InspectionAssertions::assertUnknownStep),
                args("003", a -> a.assertUnknownKey("trash")),
                args("004", InspectionAssertions::assertValueRequired),
                args("005", a -> a.assertUnknownKey("trash"))
        );
    }

    @ParameterizedTest(name = "parallel/{0}")
    @MethodSource("parallelCases")
    void testParallel(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("parallel", file, assertions);
    }

    // --- forms ---

    static Stream<Arguments> formsCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertArrayRequired),
                args("002", InspectionAssertions::assertValueRequired),
                args("003", InspectionAssertions::assertObjectRequired),
                args("004", InspectionAssertions::assertValueRequired),
                args("005", a -> a.assertUnexpectedKey("error")),
                args("006", InspectionAssertions::assertStringValueExpected),
                args("007", a -> a.assertUnexpectedValue("123")),
                args("008", a -> a.assertMissingKey("type")),
                args("009", a -> a.assertUnexpectedValue("123")),
                args("010", a -> a.assertUnexpectedValue("1")),
                args("011", InspectionAssertions::assertObjectRequired),
                args("012", a -> a.assertUnexpectedKey("min").assertUnexpectedKey("max"))
        );
    }

    @ParameterizedTest(name = "forms/{0}")
    @MethodSource("formsCases")
    void testForms(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("forms", file, assertions);
    }

    // --- configuration ---

    static Stream<Arguments> configurationCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertValueRequired),
                args("002", a -> a.assertStringValueExpected().assertUndefinedFlow()),
                args("003", InspectionAssertions::assertValueRequired),
                args("004", InspectionAssertions::assertStringValueExpected),
                args("005", InspectionAssertions::assertValueRequired),
                args("005_1", InspectionAssertions::assertValueRequired),
                args("006", a -> a.assertUnknownKey("trash")),
                args("007", InspectionAssertions::assertObjectRequired),
                args("008", InspectionAssertions::assertObjectRequired),
                args("009", InspectionAssertions::assertDurationExpected),
                args("010", InspectionAssertions::assertObjectRequired),
                args("011", a -> a.assertMissingKey("group")),
                args("011_1", a -> a.assertHasError(ConcordBundle.message("StringType.error.empty.scalar.value"))),
                args("012", a -> a.assertUnknownKey("mode1")),
                args("013", a -> a.assertUnexpectedValue("canceL")),
                args("014", InspectionAssertions::assertObjectRequired),
                args("015", InspectionAssertions::assertArrayRequired),
                args("016", a -> a.assertUnexpectedValue("1")),
                args("017", InspectionAssertions::assertArrayRequired),
                args("018", a -> a.assertUnknownKey("trash")),
                args("019", InspectionAssertions::assertArrayRequired),
                args("020", a -> a.assertSingleValueExpected().assertSingleValueExpected()),
                args("021", a -> a.assertUnexpectedValue("1")),
                args("022", InspectionAssertions::assertArrayRequired),
                args("023", InspectionAssertions::assertDurationExpected)
        );
    }

    @ParameterizedTest(name = "configuration/{0}")
    @MethodSource("configurationCases")
    void testConfiguration(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("configuration", file, assertions);
    }

    // --- formCall ---

    static Stream<Arguments> formCallCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertStringValueExpected),
                args("002", a -> a.assertUnexpectedKey("a").assertUnexpectedKey("b")),
                args("003", InspectionAssertions::assertBooleanExpected),
                args("004", InspectionAssertions::assertValueRequired),
                args("005", InspectionAssertions::assertObjectRequired),
                args("006", InspectionAssertions::assertObjectRequired),
                args("007", InspectionAssertions::assertExpressionExpected)
        );
    }

    @ParameterizedTest(name = "formCall/{0}")
    @MethodSource("formCallCases")
    void testFormCall(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("formCall", file, assertions);
    }

    // --- flows ---

    static Stream<Arguments> flowsCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertObjectRequired),
                args("002", InspectionAssertions::assertValueRequired),
                args("003", InspectionAssertions::assertArrayRequired)
        );
    }

    @ParameterizedTest(name = "flows/{0}")
    @MethodSource("flowsCases")
    void testFlows(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("flows", file, assertions);
    }

    // --- profiles ---

    static Stream<Arguments> profilesCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertObjectRequired),
                args("002", InspectionAssertions::assertObjectRequired),
                args("003", a -> a.assertUnknownKey("trash"))
        );
    }

    @ParameterizedTest(name = "profiles/{0}")
    @MethodSource("profilesCases")
    void testProfiles(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("profiles", file, assertions);
    }

    // --- if ---

    static Stream<Arguments> ifCases() {
        return Stream.of(
                args("000", a -> a.assertValueRequired().assertMissingKey("then")),
                args("001", a -> a.assertExpressionExpected().assertMissingKey("then")),
                args("002", a -> a.assertMissingKey("then")),
                args("003", InspectionAssertions::assertValueRequired),
                args("004", a -> a.assertUnknownKey("el")),
                args("005", InspectionAssertions::assertValueRequired),
                args("006", a -> a.assertUnknownKey("trash")),
                args("007", InspectionAssertions::assertValueRequired),
                args("008", InspectionAssertions::assertArrayRequired)
        );
    }

    @ParameterizedTest(name = "if/{0}")
    @MethodSource("ifCases")
    void testIf(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("if", file, assertions);
    }

    // --- switch ---

    static Stream<Arguments> switchCases() {
        return Stream.of(
                args("000", a -> a.assertValueRequired().assertHasError(ConcordBundle.message("SwitchStepMetaType.error.missing.labels"))),
                args("001", a -> a.assertHasError(ConcordBundle.message("SwitchStepMetaType.error.missing.labels"))),
                args("002", InspectionAssertions::assertValueRequired),
                args("003", InspectionAssertions::assertValueRequired)
        );
    }

    @ParameterizedTest(name = "switch/{0}")
    @MethodSource("switchCases")
    void testSwitch(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("switch", file, assertions);
    }

    // --- publicFlows ---

    static Stream<Arguments> publicFlowsCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertArrayRequired),
                args("002", InspectionAssertions::assertStringValueExpected)
        );
    }

    @ParameterizedTest(name = "publicFlows/{0}")
    @MethodSource("publicFlowsCases")
    void testPublicFlows(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("publicFlows", file, assertions);
    }

    // --- scripts ---

    static Stream<Arguments> scriptsCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", InspectionAssertions::assertStringValueExpected),
                args("002", a -> a.assertUnknownKey("body1")),
                args("003", InspectionAssertions::assertUnknownStep)
        );
    }

    @ParameterizedTest(name = "scripts/{0}")
    @MethodSource("scriptsCases")
    void testScripts(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("scripts", file, assertions);
    }

    // --- resources ---

    static Stream<Arguments> resourcesCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", a -> a.assertUnknownKey("trash")),
                args("002", InspectionAssertions::assertArrayRequired)
        );
    }

    @ParameterizedTest(name = "resources/{0}")
    @MethodSource("resourcesCases")
    void testResources(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("resources", file, assertions);
    }

    // --- setVariables ---

    static Stream<Arguments> setVariablesCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertValueRequired),
                args("001", a -> a.assertUnknownKey("meta1"))
        );
    }

    @ParameterizedTest(name = "setVariables/{0}")
    @MethodSource("setVariablesCases")
    void testSetVariables(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("setVariables", file, assertions);
    }

    // --- steps ---

    static Stream<Arguments> stepsCases() {
        return Stream.of(
                args("000", InspectionAssertions::assertUnknownStep)
        );
    }

    @ParameterizedTest(name = "steps/{0}")
    @MethodSource("stepsCases")
    void testSteps(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("steps", file, assertions);
    }

    // --- flowCallInputParams ---

    static Stream<Arguments> flowCallInputParamsCases() {
        return Stream.of(
                args("000", a -> a.assertInvalidValue(STRING_OR_EXPRESSION)
                        .assertInvalidValue(BOOLEAN_OR_EXPRESSION)
                        .assertInvalidValue(NUMBER_OR_EXPRESSION)
                        .assertInvalidValue(OBJECT_ARRAY_OR_EXPRESSION)
                        .assertInvalidValue(OBJECT_OR_EXPRESSION)
                        .assertInvalidValue(STRING_OR_EXPRESSION))
        );
    }

    @ParameterizedTest(name = "flowCallInputParams/{0}")
    @MethodSource("flowCallInputParamsCases")
    void testFlowCallInputParams(String file, Consumer<InspectionAssertions> assertions) {
        runErrorTest("flowCallInputParams", file, assertions);
    }
}
