// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface ConcordFile extends PsiFile {

    List<String> PROJECT_ROOT_FILE_NAMES = List.of(
            "concord.yml",
            ".concord.yml",
            "concord.yaml",
            ".concord.yaml"
    );

    static boolean isRootFileName(String name) {
        return PROJECT_ROOT_FILE_NAMES.contains(name);
    }

    static @Nullable VirtualFile findRootFile(@NotNull VirtualFile directory) {
        for (var name : PROJECT_ROOT_FILE_NAMES) {
            var child = directory.findChild(name);
            if (child != null) {
                return child;
            }
        }
        return null;
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
