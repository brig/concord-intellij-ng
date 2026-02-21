// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportElementMetaType extends IdentityElementMetaType implements HighlightProvider {

    private static final List<IdentityMetaType> entries = List.of(
            new ImportMetaType("dir", DirImportEntryMetaType.getInstance()),
            new ImportMetaType("git", GitImportEntryMetaType.getInstance()),
            new ImportMetaType("mvn", MvnImportEntryMetaType.getInstance())
    );

    private static final ImportElementMetaType INSTANCE = new ImportElementMetaType();

    public static ImportElementMetaType getInstance() {
        return INSTANCE;
    }

    private ImportElementMetaType() {
        super(entries);
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KIND;
    }

    private static class ImportMetaType extends IdentityMetaType {

        private final Map<String, YamlMetaType> features;

        protected ImportMetaType(String identity, YamlMetaType entry) {
            super(identity);

            this.features = Map.of(identity, entry);
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }

        @Override
        public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
            if (existingFields.contains(getIdentity())) {
                return List.of();
            }
            return List.of(getIdentity());
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
