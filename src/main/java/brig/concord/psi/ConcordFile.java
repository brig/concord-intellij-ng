// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public interface ConcordFile extends PsiFile {

    Set<String> PROJECT_ROOT_FILE_NAMES = Set.of(
            ".concord.yml",
            "concord.yml",
            ".concord.yaml",
            "concord.yaml"
    );

    static boolean isRootFileName(String name) {
        return PROJECT_ROOT_FILE_NAMES.contains(name);
    }

    static boolean isConcordFileName(@NotNull String name) {
        if (name.endsWith(".concord.yml") || name.endsWith(".concord.yaml")) {
            return true;
        }

        return isRootFileName(name);
    }

    Optional<YAMLDocument> getDocument();

    Optional<YAMLKeyValue> configuration();

    Optional<YAMLKeyValue> flows();

    Optional<YAMLKeyValue> forms();

    Optional<YAMLKeyValue> profiles();

    Optional<YAMLKeyValue> resources();

    Optional<YAMLKeyValue> imports();

    Optional<YAMLKeyValue> publicFlows();

    Optional<YAMLKeyValue> triggersKv();

    Optional<YAMLSequence> triggers();
}
