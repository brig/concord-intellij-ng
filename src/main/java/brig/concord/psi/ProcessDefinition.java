package brig.concord.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessDefinition {

    private static final String DEFAULT_CONCORD_RESOURCES = "glob:concord/{**/,}{*.,}concord.{yml,yaml}";

    private final YAMLDocument rootDoc;

    public ProcessDefinition(YAMLDocument rootDoc) {
        this.rootDoc = rootDoc;
    }

    @Nullable
    public YAMLSequence resources(String name) {
        return YamlPsiUtils.get(rootDoc, YAMLSequence.class, "resources", name);
    }

    @Nullable
    public PsiElement flow(String name) {
        return allDefinitions().stream()
                .map(d -> flow(d, name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public Set<String> flowNames() {
        Set<String> result = new HashSet<>();
        allDefinitions().forEach(c -> result.addAll(flowNames(c)));
        return result;
    }

    private static Set<String> flowNames(YAMLDocument root) {
        YAMLMapping flows = YamlPsiUtils.get(root, YAMLMapping.class, "flows");
        if (flows == null) {
            return Collections.emptySet();
        }
        return YamlPsiUtils.keys(flows);
    }

    private static PsiElement flow(PsiElement root, String name) {
        return YamlPsiUtils.get(root, YAMLPsiElement.class, "flows", name);
    }

    public List<YAMLDocument> allDefinitions() {
        List<PathMatcher> resources = resourcePatterns("concord");
        return Stream.concat(
                        FileUtils.findFiles(rootDoc.getProject(), resources).stream() // TODO: sort
                                .map(e -> PsiTreeUtil.getChildOfType(e, YAMLDocument.class))
                                .filter(Objects::nonNull),
                        Stream.of(rootDoc))
                .collect(Collectors.toList());
    }

    public List<PathMatcher> resourcePatterns(String name) {
        String rootYamlUrl = rootDoc.getContainingFile().getVirtualFile().getPresentableUrl();
        String rootYamlPath = Paths.get(rootYamlUrl).getParent().toString();

        YAMLSequence element = resources(name);
        if (element == null) {
            return Collections.singletonList(parsePattern(rootYamlPath, DEFAULT_CONCORD_RESOURCES));
        }

        return element.getItems().stream()
                .map(YAMLSequenceItem::getValue)
                .filter(v -> (v instanceof YAMLScalar))
                .map(v -> ((YAMLScalar) v).getTextValue())
                .map(v -> parsePattern(rootYamlPath, v))
                .collect(Collectors.toList());
    }

    private static PathMatcher parsePattern(String baseDir, String pattern) {
        String normalizedPattern = null;

        pattern = pattern.trim();

        if (pattern.startsWith("glob:")) {
            normalizedPattern = "glob:" + concat(baseDir, pattern.substring("glob:".length()));
        } else if (pattern.startsWith("regex:")) {
            normalizedPattern = "regex:" + concat(baseDir, pattern.substring("regex:".length()));
        }

        if (normalizedPattern != null) {
            return FileSystems.getDefault().getPathMatcher(normalizedPattern);
        }

        String singleFilePattern = concat(baseDir, pattern);
        return path -> {
//            try {
//                return Files.isSameFile(singleFilePattern, path);
                return singleFilePattern.equals(path.toAbsolutePath().toString());
//            } catch (IOException e) {
//                return false;
//            }
        };
    }

    private static String concat(String path, String str) {
        String separator = "/";
        if (str.startsWith("/")) {
            separator = "";
        }
        return path + separator + str;
    }
}
