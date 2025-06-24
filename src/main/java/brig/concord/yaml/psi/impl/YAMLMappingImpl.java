package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YamlPsiElementVisitor;

import java.util.Collection;

public abstract class YAMLMappingImpl extends YAMLCompoundValueImpl implements YAMLMapping {
    public YAMLMappingImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Unmodifiable @NotNull Collection<YAMLKeyValue> getKeyValues() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, YAMLKeyValue.class);
    }

    @Override
    public @Nullable YAMLKeyValue getKeyValueByKey(@NotNull String keyText) {
        for (YAMLKeyValue keyValue : getKeyValues()) {
            if (keyText.equals(keyValue.getKeyText())) {
                return keyValue;
            }
        }
        return null;
    }

    @Override
    public void putKeyValue(@NotNull YAMLKeyValue keyValueToAdd) {
        final YAMLKeyValue existingKey = getKeyValueByKey(keyValueToAdd.getKeyText());
        if (existingKey == null) {
            addNewKey(keyValueToAdd);
        }
        else {
            existingKey.replace(keyValueToAdd);
        }
    }

    @Override
    public void deleteKeyValue(@NotNull YAMLKeyValue keyValueToDelete) {
        if (keyValueToDelete.getParent() != this) {
            throw new IllegalArgumentException("KeyValue should be the child of this");
        }

        YAMLUtil.deleteSurroundingWhitespace(keyValueToDelete);

        keyValueToDelete.delete();
    }

    protected abstract void addNewKey(@NotNull YAMLKeyValue key);

    @Override
    public String toString() {
        return "YAML mapping";
    }

    @Override
    public @NotNull String getTextValue() {
        return "<mapping:" + Integer.toHexString(getText().hashCode()) + ">";
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitMapping(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
