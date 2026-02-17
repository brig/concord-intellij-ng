// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.openapi.command.WriteCommandAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class YAMLSequenceAddItemTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testAddItemToExistingSequence() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                """);

        addSequenceItem("/configuration/dependencies", "\"mvn://com.example:other:2.0.0\"");

        myFixture.checkResult("""
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                    - "mvn://com.example:other:2.0.0"
                """);
    }

    @Test
    void testAddItemPreservesExistingFormatting() {
        configureFromText("""
                configuration:
                  dependencies:
                    - 'mvn://com.example:lib:1.0.0'
                    - mvn://com.example:unquoted:1.0.0
                """);

        addSequenceItem("/configuration/dependencies", "\"mvn://com.example:other:2.0.0\"");

        myFixture.checkResult("""
                configuration:
                  dependencies:
                    - 'mvn://com.example:lib:1.0.0'
                    - mvn://com.example:unquoted:1.0.0
                    - "mvn://com.example:other:2.0.0"
                """);
    }

    @Test
    void testAddItemToMultipleExistingItems() {
        configureFromText("""
                configuration:
                  dependencies:
                    - "mvn://com.example:a:1.0"
                    - "mvn://com.example:b:2.0"
                    - "mvn://com.example:c:3.0"
                """);

        addSequenceItem("/configuration/dependencies", "\"mvn://com.example:d:4.0\"");

        myFixture.checkResult("""
                configuration:
                  dependencies:
                    - "mvn://com.example:a:1.0"
                    - "mvn://com.example:b:2.0"
                    - "mvn://com.example:c:3.0"
                    - "mvn://com.example:d:4.0"
                """);
    }

    @Test
    void testAddItemToTopLevelSequence() {
        configureFromText("""
                items:
                  - one
                """);

        addSequenceItem("/items", "two");

        myFixture.checkResult("""
                items:
                  - one
                  - two
                """);
    }

    @Test
    void testAddItemToDeeplyNestedSequence() {
        configureFromText("""
                profiles:
                  idea:
                    configuration:
                      dependencies:
                        - "mvn://com.example:lib:1.0.0"
                """);

        addSequenceItem("/profiles/idea/configuration/dependencies", "\"mvn://com.example:other:2.0.0\"");

        myFixture.checkResult("""
                profiles:
                  idea:
                    configuration:
                      dependencies:
                        - "mvn://com.example:lib:1.0.0"
                        - "mvn://com.example:other:2.0.0"
                """);
    }

    private void addSequenceItem(String sequencePath, String valueText) {
        var element = yamlPath.valueElement(sequencePath);
        assertInstanceOf(YAMLSequence.class, element, "Expected YAMLSequence at " + sequencePath);
        var seq = (YAMLSequence) element;
        var generator = YAMLElementGenerator.getInstance(getProject());
        var newItem = generator.createSequenceItem(valueText);
        WriteCommandAction.runWriteCommandAction(getProject(), () -> seq.addItem(newItem));
    }
}
