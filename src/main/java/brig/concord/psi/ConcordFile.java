package brig.concord.psi;

import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.psi.PsiFile;

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

    Optional<YAMLDocument> getDocument();

    Optional<YAMLKeyValue> configuration();

    Optional<YAMLKeyValue> flows();

    Optional<YAMLKeyValue> forms();

    Optional<YAMLKeyValue> profiles();

    Optional<YAMLKeyValue> resources();

    Optional<YAMLKeyValue> imports();

    Optional<YAMLKeyValue> publicFlows();

    Optional<YAMLKeyValue> triggers();
}
