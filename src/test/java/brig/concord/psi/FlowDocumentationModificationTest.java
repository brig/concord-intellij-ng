package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.command.WriteCommandAction;
import org.junit.jupiter.api.Test;

public class FlowDocumentationModificationTest extends ConcordYamlTestBase {

    @Test
    public void testAddInputParameterToExistingSection() {
        var yaml = """
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "string");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   newParam: string
              ##
              myFlow:
                - log: "Hello"
            """);
    }
    @Test
    public void testAddInputParameterToNewSection() {
        var yaml = """
            flows:
              ##
              # My Description
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "string");
        });

        myFixture.checkResult("""
            flows:
              ##
              # My Description
              # in:
              #   newParam: string
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddInputParameterToEmptyDoc() {
        var yaml = """
            flows:
              ##
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "string");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   newParam: string
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddMultipleParametersSequentially() {
        var yaml = """
            flows:
              ##
              # in:
              #   first: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("second", "int");
            flowDoc.addInputParameter("third", "boolean");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   first: string
              #   second: int
              #   third: boolean
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWithArrayType() {
        var yaml = """
            flows:
              ##
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("items", "string[]");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   items: string[]
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWhenOutSectionExists() {
        var yaml = """
            flows:
              ##
              # out:
              #   result: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("input", "string");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   input: string
              # out:
              #   result: string
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWithExistingInAndOutSections() {
        var yaml = """
            flows:
              ##
              # in:
              #   existing: string
              # out:
              #   result: int
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newInput", "boolean");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing: string
              #   newInput: boolean
              # out:
              #   result: int
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterToSectionWithMultipleExistingParams() {
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string
              #   param2: int
              #   param3: boolean
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("param4", "object");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   param1: string
              #   param2: int
              #   param3: boolean
              #   param4: object
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWithDescriptionAndOutSection() {
        var yaml = """
            flows:
              ##
              # Process data from source
              # out:
              #   processed: int
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("source", "string");
        });

        myFixture.checkResult("""
            flows:
              ##
              # Process data from source
              # in:
              #   source: string
              # out:
              #   processed: int
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWithDifferentIndentation() {
        // Test with 4-space indentation
        var yaml = """
            flows:
                ##
                # in:
                #   existing: string
                ##
                myFlow:
                    - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "int");
        });

        myFixture.checkResult("""
            flows:
                ##
                # in:
                #   existing: string
                #   newParam: int
                ##
                myFlow:
                    - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterIntoEmptyInSection() {
        var yaml = """
            flows:
                ##
                # in:
                ##
                myFlow:
                    - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "int");
        });

        myFixture.checkResult("""
            flows:
                ##
                # in:
                #   newParam: int
                ##
                myFlow:
                    - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterIntoIndentedInSection() {
        var yaml = """
            flows:
              ##
              #     in:
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "int");
        });

        myFixture.checkResult("""
            flows:
              ##
              #     in:
              #       newParam: int
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterIntoIndentedInSectionWithParam() {
        var yaml = """
            flows:
              ##
              #     in:
              #             source: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "int");
        });

        myFixture.checkResult("""
            flows:
              ##
              #     in:
              #             source: string
              #             newParam: int
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWithMultilineDescription() {
        var yaml = """
            flows:
              ##
              # This is a long description
              # that spans multiple lines
              # and explains what the flow does
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("input", "string");
        });

        myFixture.checkResult("""
            flows:
              ##
              # This is a long description
              # that spans multiple lines
              # and explains what the flow does
              # in:
              #   input: string
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWithHyphenInName() {
        var yaml = """
            flows:
              ##
              # in:
              #   existing-param: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("new-param", "int");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing-param: string
              #   new-param: int
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWithUnderscoreInName() {
        var yaml = """
            flows:
              ##
              # in:
              #   existing_param: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("new_param", "int");
        });

        myFixture.checkResult("""
            flows:
              ##
              # in:
              #   existing_param: string
              #   new_param: int
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWhenOutSectionExistsWithDescription() {
        // Both description and out: exist, but no in: - should insert in: between them
        var yaml = """
            flows:
              ##
              # My flow description
              # out:
              #   result: boolean
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("input", "string");
        });

        myFixture.checkResult("""
            flows:
              ##
              # My flow description
              # in:
              #   input: string
              # out:
              #   result: boolean
              ##
              myFlow:
                - log: "Hello"
            """);
    }

    @Test
    public void testAddParameterWhenOutBeforeIn() {
        // Unusual order: out: section comes before in: section
        var yaml = """
            flows:
              ##
              # out:
              #   result: boolean
              # in:
              #   existing: string
              ##
              myFlow:
                - log: "Hello"
            """;
        configureFromText(yaml);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var flowDoc = flowDoc("/flows/myFlow").getFlowDoc();
            flowDoc.addInputParameter("newParam", "int");
        });

        myFixture.checkResult("""
            flows:
              ##
              # out:
              #   result: boolean
              # in:
              #   existing: string
              #   newParam: int
              ##
              myFlow:
                - log: "Hello"
            """);
    }
}
