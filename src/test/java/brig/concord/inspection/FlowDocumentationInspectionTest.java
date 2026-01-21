package brig.concord.inspection;

import brig.concord.assertions.FlowDocAssertions;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class FlowDocumentationInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(FlowDocumentationInspection.class);
    }

    @Test
    public void testValidFlowDocumentation() {
        configureFromText("""
            flows:
              ##
              # Process S3 files
              # in:
              #   bucket: string, mandatory
              #   count: int, optional
              # out:
              #   processed: boolean, mandatory
              ##
              processS3:
                - task: s3
            """);

        inspection(doc()).expectNoErrors();
    }

    @Test
    public void testDuplicateParameterInInSection() {
        configureFromText("""
            flows:
              ##
              # in:
              #   bucket: string, mandatory
              #   bucket: int, optional
              ##
              processS3:
                - task: s3
            """);

        inspection(flowDocParam("/flows/processS3", "bucket", 2))
                .expectDuplicateFlowDocParam("bucket", "in");
    }

    @Test
    public void testDuplicateParameterInOutSection() {
        configureFromText("""
            flows:
              ##
              # out:
              #   result: string, mandatory
              #   result: int, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        inspection(flowDocParam("/flows/myFlow", "result", 2))
                .expectDuplicateFlowDocParam("result", "out");
    }

    @Test
    public void testUnknownType() {
        configureFromText("""
            flows:
              ##
              # in:
              #   bucket: foo, mandatory
              ##
              processS3:
                - task: s3
            """);

        inspection(flowDocParam("/flows/processS3", "bucket"))
                .expectUnknownType("foo");
    }

    @Test
    public void testUnknownArrayType() {
        configureFromText("""
            flows:
              ##
              # in:
              #   items: bar[], mandatory
              ##
              myFlow:
                - log: "test"
            """);

        inspection(flowDocParam("/flows/myFlow", "items"))
                .expectUnknownType("bar[]");
    }

    @Test
    public void testAllValidTypes() {
        configureFromText("""
            flows:
              ##
              # in:
              #   a: string, mandatory
              #   b: boolean, mandatory
              #   c: int, mandatory
              #   d: integer, mandatory
              #   e: number, mandatory
              #   f: object, mandatory
              #   g: any, mandatory
              #   h: string[], mandatory
              #   i: boolean[], mandatory
              #   j: int[], mandatory
              #   k: integer[], mandatory
              #   l: number[], mandatory
              #   m: object[], mandatory
              #   n: any[], mandatory
              ##
              allTypes:
                - log: "test"
            """);

        inspection(doc()).expectNoErrors();
    }

    @Test
    public void testOrphanedDocumentation() {
        configureFromText("""
            flows:
              ##
              # in:
              #   bucket: string, mandatory
              ##
            """);

        inspection(flowDocByIndex(0))
                .expectOrphanedFlowDoc();
    }

    @Test
    public void testMultipleDuplicateParameters() {
        configureFromText("""
            flows:
              ##
              # in:
              #   a: string, mandatory
              #   a: int, mandatory
              #   a: boolean, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        // First occurrence is OK
        inspection(flowDocParam("/flows/myFlow", "a", 1)).expectNoErrors();
        // Second and third are duplicates
        inspection(flowDocParam("/flows/myFlow", "a", 2))
                .expectDuplicateFlowDocParam("a", "in");
        inspection(flowDocParam("/flows/myFlow", "a", 3))
                .expectDuplicateFlowDocParam("a", "in");
    }

    @Test
    public void testSameParameterInDifferentSections_isOk() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: string, mandatory
              # out:
              #   param: string, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        // Same name in different sections is allowed
        inspection(doc()).expectNoErrors();
    }

    @Test
    public void testDocumentationWithOnlyDescription_isOk() {
        configureFromText("""
            flows:
              ##
              # This is just a description
              # No parameters here
              ##
              myFlow:
                - log: "test"
            """);

        inspection(doc()).expectNoErrors();
    }

    @Test
    public void testUnknownKeyword_typoInMandatory() {
        configureFromText("""
            flows:
              ##
              # in:
              #   bucket: string, mandatry
              ##
              myFlow:
                - log: "test"
            """);

        inspection(unknownKeyword("/flows/myFlow", "bucket"))
                .expectUnknownKeyword("mandatry");
    }

    @Test
    public void testUnknownKeyword_typoInOptional() {
        configureFromText("""
            flows:
              ##
              # in:
              #   bucket: string, optinal
              ##
              myFlow:
                - log: "test"
            """);

        inspection(unknownKeyword("/flows/myFlow", "bucket"))
                .expectUnknownKeyword("optinal");
    }

    @Test
    public void testUnknownKeyword_wrongWord() {
        configureFromText("""
            flows:
              ##
              # in:
              #   bucket: string, requred
              ##
              myFlow:
                - log: "test"
            """);

        inspection(unknownKeyword("/flows/myFlow", "bucket"))
                .expectUnknownKeyword("requred");
    }

    @Test
    public void testSameParamInOut() {
        configureFromText("""
            flows:
              ##
              #  in:
              #    outFile: string, optional, interpolated file name
              #  out:
              #    outFile: string, optional, path to interpolated file
              ##
              myFlow:
                - log: "test: ${bucket}"
            """);

        flowDocFor(key("/flows/myFlow"), doc -> doc
                .hasFlowName("myFlow")
                .hasInputCount(1)
                .hasOutputCount(1));

        inspection(doc()).expectNoWarnings();
    }

    private void flowDocFor(KeyTarget flowKey, Consumer<FlowDocAssertions> assertions) {
        FlowDocAssertions.assertFlowDoc(yamlPath, flowKey, assertions);
    }
}
