package brig.concord.yaml.meta.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.codeInsight.completion.InsertionContext;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlMetaType.YamlInsertionMarkup;

public class YamlKeyInsertHandlerImpl extends YamlKeyInsertHandler {
    private final Field myToBeInserted;

    public YamlKeyInsertHandlerImpl(boolean needsSequenceItemMark, @NotNull Field toBeInserted) {
        super(needsSequenceItemMark);
        myToBeInserted = toBeInserted;
    }

    @Override
    protected @NotNull YamlInsertionMarkup computeInsertionMarkup(@NotNull InsertionContext context) {
        YamlInsertionMarkup markup = new YamlInsertionMarkup(context);
        Field.Relation relation = myToBeInserted.getDefaultRelation();
        YamlMetaType defaultType = myToBeInserted.getType(relation);
        defaultType.buildInsertionSuffixMarkup(markup, relation);
        return markup;
    }
}