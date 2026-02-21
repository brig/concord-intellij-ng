// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import brig.concord.meta.DynamicMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlMetaType;
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
        var schema = findTaskSchema(element);
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

    static @Nullable String findTaskName(@NotNull PsiElement element) {
        var mapping = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, false);
        if (mapping == null) {
            return null;
        }

        // element may be inside the in: mapping; step mapping is one level up
        var stepMapping = mapping.getKeyValueByKey("task") != null
                ? mapping
                : YamlPsiUtils.getParentOfType(mapping, YAMLMapping.class, false);
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
