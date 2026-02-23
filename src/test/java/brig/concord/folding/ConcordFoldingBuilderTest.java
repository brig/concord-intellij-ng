// SPDX-License-Identifier: Apache-2.0
package brig.concord.folding;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.assertions.FoldRegionAssert;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class ConcordFoldingBuilderTest extends ConcordYamlTestBaseJunit5 {

    // -- top-level sections --

    @Test
    void testTopLevelSectionsFold() {
        configureFromText("""
                configuration:
                  arguments:
                    x: 1
                flows:
                  default:
                    - log: hello
                triggers:
                  - cron:
                      spec: "0 12 * * *"
                """);

        foldRegion(kv("/configuration")).assertPlaceholderText("configuration: ...");
        foldRegion(kv("/flows")).assertPlaceholderText("flows: ...");
        foldRegion(kv("/triggers")).assertPlaceholderText("triggers: ...");
    }

    @Test
    void testImportsSectionFolds() {
        configureFromText("""
                imports:
                  - git:
                      url: "https://github.com/example/repo"
                      version: "main"
                  - dir:
                      src: /src
                """);

        foldRegion(kv("/imports")).assertPlaceholderText("imports: ...");
    }

    @Test
    void testFormsSectionFolds() {
        configureFromText("""
                forms:
                  myForm:
                    - x: {type: "string"}
                    - y: {type: "int"}
                """);

        foldRegion(kv("/forms")).assertPlaceholderText("forms: ...");
    }

    @Test
    void testProfilesSectionFolds() {
        configureFromText("""
                profiles:
                  staging:
                    configuration:
                      arguments:
                        env: staging
                """);

        foldRegion(kv("/profiles")).assertPlaceholderText("profiles: ...");
    }

    // -- configuration --

    @Test
    void testConfigurationSubKeysFold() {
        configureFromText("""
                configuration:
                  arguments:
                    x: 1
                    y: 2
                  meta:
                    key: value
                """);

        foldRegion(kv("/configuration/arguments")).assertPlaceholderText("arguments: ...");
        foldRegion(kv("/configuration/meta")).assertPlaceholderText("meta: ...");
    }

    @Test
    void testDeepConfigDoesNotFold() {
        configureFromText("""
                configuration:
                  arguments:
                    nested:
                      deep: value
                """);

        // arguments should fold
        foldRegion(kv("/configuration/arguments")).assertPlaceholderText("arguments: ...");
        // but nested inside arguments should NOT have a fold region
        assertNoFoldRegion(kv("/configuration/arguments/nested"));
    }

    // -- flows --

    @Test
    void testFlowDefinitionPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - log: hello
                    - log: world
                    - log: done
                """);

        foldRegion(kv("/flows/default")).assertPlaceholderText("default: ...");
    }

    @Test
    void testFlowWithSingleStep() {
        configureFromText("""
                flows:
                  simple:
                    - log: hello
                """);

        foldRegion(kv("/flows/simple")).assertPlaceholderText("simple: ...");
    }

    // -- step types --

    @Test
    void testTaskStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - task: myTask
                      in:
                        param: value
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- task: myTask");
    }

    @Test
    void testCallStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - call: otherFlow
                      in:
                        x: 1
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- call: otherFlow");
    }

    @Test
    void testIfStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - if: ${myVar}
                      then:
                        - log: yes
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- if: ${myVar}");
    }

    @Test
    void testTryStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - try:
                        - log: attempt
                        - log: attempt2
                      error:
                        - log: failed
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- try: ...");
    }

    @Test
    void testBlockStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - block:
                        - log: one
                        - log: two
                        - log: three
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- block: ...");
    }

    @Test
    void testParallelStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - parallel:
                        - log: one
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- parallel: ...");
    }

    @Test
    void testSetStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - set:
                        x: 1
                        y: 2
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- set: ...");
    }

    @Test
    void testScriptStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - script: js
                      body: |
                        print("hello")
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- script: js");
    }

    @Test
    void testSwitchStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - switch: ${action}
                      a:
                        - log: a
                      b:
                        - log: b
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- switch: ${action}");
    }

    @Test
    void testLogStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - log: "hello world message"
                      if: ${cond}
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- log: hello world message");
    }

    // -- step sub-keys --

    @Test
    void testStepSubKeysFold() {
        configureFromText("""
                flows:
                  default:
                    - task: myTask
                      in:
                        param1: val1
                        param2: val2
                      error:
                        - log: error
                        - log: recover
                """);

        foldRegion(kv("/flows/default[0]/in")).assertPlaceholderText("in: ...");
        foldRegion(kv("/flows/default[0]/error")).assertPlaceholderText("error: ...");
    }

    @Test
    void testThenElseSubKeysFold() {
        configureFromText("""
                flows:
                  default:
                    - if: ${cond}
                      then:
                        - log: yes
                      else:
                        - log: no
                """);

        foldRegion(kv("/flows/default[0]/then")).assertPlaceholderText("then: ...");
        foldRegion(kv("/flows/default[0]/else")).assertPlaceholderText("else: ...");
    }

    // -- triggers --

    @Test
    void testTriggerPlaceholders() {
        configureFromText("""
                triggers:
                  - github:
                      entryPoint: onPush
                  - cron:
                      spec: "0 12 * * *"
                      entryPoint: scheduledJob
                  - manual:
                      name: triggerIt
                """);

        foldRegion(seqItem("/triggers", 0)).assertPlaceholderText("- github: ...");
        foldRegion(seqItem("/triggers", 1)).assertPlaceholderText("- cron: ...");
        foldRegion(seqItem("/triggers", 2)).assertPlaceholderText("- manual: ...");
    }

    // -- imports --

    @Test
    void testGitImportPlaceholder() {
        configureFromText("""
                imports:
                  - git:
                      url: "https://github.com/example/repo"
                      version: "main"
                """);

        foldRegion(seqItem("/imports", 0)).assertPlaceholderText("- git: https://g\u2026ample/repo");
    }

    @Test
    void testDirImportPlaceholder() {
        configureFromText("""
                imports:
                  - dir:
                      src: /src
                """);

        foldRegion(seqItem("/imports", 0)).assertPlaceholderText("- dir: /src");
    }

    @Test
    void testMvnImportPlaceholder() {
        configureFromText("""
                imports:
                  - mvn:
                      url: "mvn://com.example:lib:1.0"
                """);

        foldRegion(seqItem("/imports", 0)).assertPlaceholderText("- mvn: mvn://com\u2026le:lib:1.0");
    }

    // -- profiles --

    @Test
    void testProfileEntryFolds() {
        configureFromText("""
                profiles:
                  staging:
                    configuration:
                      arguments:
                        env: staging
                    flows:
                      default:
                        - log: staging
                """);

        foldRegion(kv("/profiles/staging")).assertPlaceholderText("staging: ...");
        foldRegion(kv("/profiles/staging/configuration")).assertPlaceholderText("configuration: ...");
        foldRegion(kv("/profiles/staging/flows")).assertPlaceholderText("flows: ...");
    }

    @Test
    void testProfileFlowsFoldLikeTopLevel() {
        configureFromText("""
                profiles:
                  staging:
                    flows:
                      deploy:
                        - log: deploying
                        - task: deploy
                          in:
                            env: staging
                """);

        foldRegion(kv("/profiles/staging/flows/deploy")).assertPlaceholderText("deploy: ...");
        foldRegion(seqItem("/profiles/staging/flows/deploy", 1)).assertPlaceholderText("- task: deploy");
    }

    // -- forms --

    @Test
    void testFormEntryPlaceholder() {
        configureFromText("""
                forms:
                  myForm:
                    - x: {type: "string"}
                    - y: {type: "int"}
                    - z: {type: "boolean"}
                """);

        foldRegion(kv("/forms/myForm")).assertPlaceholderText("myForm: ...");
    }

    // -- additional step types --

    @Test
    void testExprStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - expr: ${result}
                      out: myVar
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- expr: ${result}");
    }

    @Test
    void testThrowStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - throw: "something went wrong"
                      name: myError
                """);

        // "something went wrong" is exactly 20 chars, no truncation
        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- throw: something went wrong");
    }

    @Test
    void testSuspendStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - suspend: waitForApproval
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- suspend: waitForApproval");
    }

    @Test
    void testCheckpointStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - checkpoint: myCheckpoint
                      meta:
                        key: value
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- checkpoint: myCheckpoint");
    }

    @Test
    void testFormStepPlaceholder() {
        configureFromText("""
                flows:
                  default:
                    - form: myForm
                      yield: true
                """);

        foldRegion(seqItem("/flows/default", 0)).assertPlaceholderText("- form: myForm");
    }

    // -- form field items --

    @Test
    void testFormFieldItemsFold() {
        configureFromText("""
                forms:
                  myForm:
                    - x: {type: "string", label: "Name"}
                    - y: {type: "int", label: "Age"}
                """);

        // Form field items (with mapping values) should be folded
        foldRegion(seqItem("/forms/myForm", 0)).assertPlaceholderText("- ...");
        foldRegion(seqItem("/forms/myForm", 1)).assertPlaceholderText("- ...");
    }

    // -- negative cases --

    @Test
    void testReturnStepNotFolded() {
        configureFromText("""
                flows:
                  default:
                    - return
                """);

        // Scalar steps should not be folded
        assertNoFoldRegion(seqItem("/flows/default", 0));
    }

    @Test
    void testExitStepNotFolded() {
        configureFromText("""
                flows:
                  default:
                    - exit
                """);

        assertNoFoldRegion(seqItem("/flows/default", 0));
    }

    // -- helpers --

    private FoldRegionAssert foldRegion(AbstractTarget target) {
        return FoldRegionAssert.foldRegion(myFixture, target);
    }

    private void assertNoFoldRegion(AbstractTarget target) {
        FoldRegionAssert.assertNoFoldRegion(myFixture, target);
    }

    /**
     * Target for the entire YAMLKeyValue element at a given path.
     */
    private KvTarget kv(String path) {
        return new KvTarget(path);
    }

    /**
     * Target for a YAMLSequenceItem at a given path and index.
     */
    private SeqItemTarget seqItem(String path, int index) {
        return new SeqItemTarget(path, index);
    }

    private final class KvTarget extends AbstractTarget {

        private KvTarget(String path) {
            super(path);
        }

        @Override
        public @NotNull String text() {
            return ReadAction.compute(() -> key(path).asKeyValue().getText());
        }

        @Override
        public @NotNull TextRange range() {
            return ReadAction.compute(() -> key(path).asKeyValue().getTextRange());
        }
    }

    private final class SeqItemTarget extends AbstractTarget {
        private final int index;

        private SeqItemTarget(String path, int index) {
            super(path + "[" + index + "]");
            this.index = index;
        }

        @Override
        public @NotNull String text() {
            return ReadAction.compute(() -> getItem().getText());
        }

        @Override
        public @NotNull TextRange range() {
            return ReadAction.compute(() -> getItem().getTextRange());
        }

        private @NotNull YAMLSequenceItem getItem() {
            var seq = value(path).element();
            if (!(seq instanceof YAMLSequence sequence)) {
                throw new AssertionError("Expected sequence at path: " + path + ", got: " + seq.getClass().getSimpleName());
            }
            var items = sequence.getItems();
            if (index < 0 || index >= items.size()) {
                throw new AssertionError("Sequence item index " + index + " out of bounds (" + items.size() + ") at path: " + path);
            }
            return items.get(index);
        }
    }
}
