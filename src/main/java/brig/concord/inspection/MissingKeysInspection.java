package brig.concord.inspection;

import brig.concord.meta.ConcordMetaTypeProvider;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.impl.YamlMetaTypeProvider;
import org.jetbrains.yaml.meta.impl.YamlMissingKeysInspectionBase;

@SuppressWarnings("UnstableApiUsage")
public class MissingKeysInspection extends YamlMissingKeysInspectionBase {
    @Override
    protected @Nullable YamlMetaTypeProvider getMetaTypeProvider(@NotNull ProblemsHolder holder) {
        return ConcordMetaTypeProvider.getInstance(holder.getProject());
    }
}
