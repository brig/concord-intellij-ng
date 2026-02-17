package brig.concord.schema;

import brig.concord.meta.DynamicMetaType;
import brig.concord.meta.model.OutVarMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class TaskOutParamsMetaType extends YamlAnyOfType implements DynamicMetaType {

    private static final TaskOutParamsMetaType INSTANCE = new TaskOutParamsMetaType();

    private final boolean hasSchema;

    public static TaskOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private TaskOutParamsMetaType() {
        super(List.of(OutVarMetaType.getInstance(), AnyMapMetaType.getInstance()),
                descKey("doc.step.feature.out.description"));
        this.hasSchema = false;
    }

    private TaskOutParamsMetaType(@NotNull YamlMetaType objectType, boolean hasSchema) {
        super(OutVarMetaType.getInstance(), objectType);
        this.hasSchema = hasSchema;
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        if (hasSchema) {
            // Let the composite type delegate to TaskSchemaMetaType subtype
            return super.findFeatureByName(name);
        }
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }

    @Override
    public YamlMetaType resolve(PsiElement element) {
        var schema = TaskInParamsMetaType.findTaskSchema(element);
        if (schema == null) {
            return INSTANCE;
        }

        var outSection = schema.getOutSection();
        if (outSection.properties().isEmpty()) {
            return INSTANCE;
        }

        var metaType = new TaskSchemaMetaType(outSection, Collections.emptySet());
        return new TaskOutParamsMetaType(metaType, true);
    }

}
