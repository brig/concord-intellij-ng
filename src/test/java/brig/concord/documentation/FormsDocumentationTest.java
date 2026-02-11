package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class FormsDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteForms() {
        configureFromText("""
                flows:
                  main:
                    - log: "Hello"

                forms:
                  myForm:
                  - fullName: { label: "Name", type: "string", pattern: ".* .*", readonly: true, placeholder: "Place name here" }
                """);

//        assertDocTarget(key("/forms"), "doc.forms.description",
//                "/documentation/forms.html");
//
//        assertDocTarget(key("/forms/myForm"), "doc.forms.formName.description",
//                "/documentation/forms.formName.html");

        assertDocTarget(key("/forms/myForm[0]/fullName"), "doc.forms.formName.formField.description",
                "/documentation/forms.formName.formField.html");
    }
}
