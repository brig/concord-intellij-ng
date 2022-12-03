package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ImportElementMetaType extends IdentityElementMetaType {

    private static final List<IdentityMetaType> entries = List.of(
            new ImportMetaType("dir", DirImportEntryMetaType::getInstance),
            new ImportMetaType("git", GitImportEntryMetaType::getInstance),
            new ImportMetaType("mvn", MvnImportEntryMetaType::getInstance)
    );

    private static final ImportElementMetaType INSTANCE = new ImportElementMetaType();

    public static ImportElementMetaType getInstance() {
        return INSTANCE;
    }

    protected ImportElementMetaType() {
        super("Imports", entries);
    }

    private static class ImportMetaType extends IdentityMetaType {

        private final Map<String, Supplier<YamlMetaType>> features;

        protected ImportMetaType(String identity, Supplier<YamlMetaType> entry) {
            super(identity, identity, Set.of(identity));

            this.features = Map.of(identity, entry);
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }

        @Override
        public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
            if (!(value instanceof YAMLMapping)) {
                problemsHolder.registerProblem(value, ConcordBundle.message("ConcordMetaType.error.object.is.required"), ProblemHighlightType.ERROR);
            }
            super.validateValue(value, problemsHolder);
        }
    }
}
