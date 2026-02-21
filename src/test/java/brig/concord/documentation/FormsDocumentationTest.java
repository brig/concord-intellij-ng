// SPDX-License-Identifier: Apache-2.0
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
                  - fullName: { label: "Name", type: "string", pattern: ".* .*", readonly: true, placeholder: "Place name here", search: "true", min: 1, max: 100, unknownField: boo }
                """);

        assertDocTarget(key("/forms"), "doc.forms.description",
                "/documentation/forms.html");

        assertDocTarget(key("/forms/myForm"), "doc.forms.formName.description",
                "/documentation/forms.formName.html");

        assertDocTarget(key("/forms/myForm[0]/fullName"), "doc.forms.formName.formField.description",
                "/documentation/forms.formName.formField.html");

        assertDocTarget(key("/forms/myForm[0]/fullName/type"), "doc.forms.formName.formField.type.description",
                "/documentation/forms.formName.formField.type.html");
        assertDocTarget(key("/forms/myForm[0]/fullName/label"), "doc.forms.formName.formField.label.description",
                "/documentation/forms.formName.formField.label.html");
        assertDocTarget(key("/forms/myForm[0]/fullName/pattern"), "doc.forms.formName.formField.pattern.description",
                "/documentation/forms.formName.formField.pattern.html");
        assertDocTarget(key("/forms/myForm[0]/fullName/readonly"), "doc.forms.formName.formField.readonly.description",
                "/documentation/forms.formName.formField.readonly.html");
        assertDocTarget(key("/forms/myForm[0]/fullName/placeholder"), "doc.forms.formName.formField.placeholder.description",
                "/documentation/forms.formName.formField.placeholder.html");
        assertDocTarget(key("/forms/myForm[0]/fullName/search"), "doc.forms.formName.formField.search.description",
                "/documentation/forms.formName.formField.search.html");
        assertDocTarget(key("/forms/myForm[0]/fullName/min"), "doc.forms.formName.formField.min.description",
                "/documentation/forms.formName.formField.min.html");
        assertDocTarget(key("/forms/myForm[0]/fullName/max"), "doc.forms.formName.formField.max.description",
                "/documentation/forms.formName.formField.max.html");

        assertNoDocTarget(key("/forms/myForm[0]/fullName/unknownField"));
    }
}
