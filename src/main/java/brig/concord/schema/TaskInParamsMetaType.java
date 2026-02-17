package brig.concord.schema;

import brig.concord.meta.DynamicMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class TaskInParamsMetaType extends YamlAnyOfType implements DynamicMetaType {

    private static final TaskInParamsMetaType INSTANCE = new TaskInParamsMetaType();

    public static TaskInParamsMetaType getInstance() {
        return INSTANCE;
    }

    private TaskInParamsMetaType() {
        this(AnyMapMetaType.getInstance());
    }

    private TaskInParamsMetaType(@NotNull YamlMetaType objectType) {
        super(List.of(ExpressionMetaType.getInstance(), objectType),
                descKey("doc.step.feature.in.description"));
    }

    @Override
    public YamlMetaType resolve(PsiElement element) {
        var inMapping = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, true);
        var schema = findTaskSchema(inMapping);
        if (schema == null) {
            return INSTANCE;
        }

        var currentValues = readCurrentValues(element);
        var section = schema.resolveInSection(currentValues);
        var discriminatorKeys = schema.getDiscriminatorKeys();
        var metaType = new TaskSchemaMetaType(section, discriminatorKeys);

        return new TaskInParamsMetaType(metaType);
    }

    static @Nullable TaskSchema findTaskSchema(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }

        var taskName = findTaskName(element);
        if (taskName == null) {
            return null;
        }

        return TaskSchemaRegistry.getInstance(element.getProject()).getSchema(taskName);
    }

    /**
     * Extracts the task name from the enclosing task step.
     * Must only be called on elements that are values of {@code in:} or {@code out:}
     * keys inside a task step (i.e. elements whose resolved meta type is
     * {@link TaskInParamsMetaType} or {@link TaskOutParamsMetaType}).
     */
    static @Nullable String findTaskName(@NotNull PsiElement element) {
        // element is the value of in:/out: key -> parent is the KV -> parentMapping is the step mapping
        var parentKv = YamlPsiUtils.getParentOfType(element, YAMLKeyValue.class, false);
        if (parentKv == null) {
            return null;
        }

        var stepMapping = parentKv.getParentMapping();
        if (stepMapping == null) {
            return null;
        }

        var taskKv = stepMapping.getKeyValueByKey("task");
        if (taskKv == null) {
            return null;
        }

        return taskKv.getValue() instanceof YAMLScalar scalar ? scalar.getTextValue() : null;
    }

    static @NotNull Map<String, String> readCurrentValues(@NotNull PsiElement element) {
        var mapping = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, true);
        if (mapping == null) {
            return Collections.emptyMap();
        }

        var result = new LinkedHashMap<String, String>();
        for (var kv : mapping.getKeyValues()) {
            var key = kv.getKeyText().trim();
            var value = kv.getValue();
            if (value instanceof YAMLScalar scalar) {
                result.put(key, scalar.getTextValue());
            }
        }
        return result;
    }
}
