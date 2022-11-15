package brig.concord.meta;

import brig.concord.meta.model.IdentityElementMetaType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeProvider;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.List;
import java.util.Set;

public class ConcordMetaTypeProxy implements YamlMetaTypeProvider.MetaTypeProxy {

    private final YamlMetaTypeProxy metaTypeProxy;
    private final Field field;

    public ConcordMetaTypeProxy(PsiElement element, YamlMetaType type, Field field) {
        this.metaTypeProxy = new YamlMetaTypeProxy(element, type);
        this.field = field;
    }

    @Override
    public @NotNull YamlMetaType getMetaType() {
        return metaTypeProxy;
    }

    @Override
    public @NotNull Field getField() {
        return field;
    }

    public static class YamlMetaTypeProxy extends YamlMetaType {

        private final PsiElement element;
        private final YamlMetaType delegate;

        private YamlMetaTypeProxy(PsiElement element, YamlMetaType delegate) {
            super(delegate.getTypeName());

            this.element = element;
            this.delegate = delegate;
        }

        public YamlMetaType original() {
            return delegate;
        }

        @Override
        public @Nullable Field findFeatureByName(@NotNull String name) {
            if (delegate instanceof IdentityElementMetaType m) {
                return m.findFeatureByName(element, name);
            }
            return delegate.findFeatureByName(name);
        }

        @Override
        public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
            return delegate.computeMissingFields(existingFields);
        }

        @Override
        public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
            return delegate.computeKeyCompletions(existingMapping);
        }

        @Override
        public void buildInsertionSuffixMarkup(@NotNull YamlInsertionMarkup markup, Field.@NotNull Relation relation, ForcedCompletionPath.@NotNull Iteration iteration) {
            delegate.buildInsertionSuffixMarkup(markup, relation, iteration);
        }
    }
}
