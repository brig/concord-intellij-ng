package brig.concord.yaml.meta.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLValue;

import java.util.Arrays;
import java.util.List;

import static brig.concord.yaml.YAMLElementTypes.SCALAR_PLAIN_VALUE;

public final class YamlMetaUtil {
    /**
     * @return true if the value is a <a href="https://yaml.org/type/null.html">null</a> value
     */
    public static boolean isNull(@NotNull YAMLValue value) {
        return value.getNode().getElementType().equals(SCALAR_PLAIN_VALUE) && NULL_VALUES.contains(value.getText());
    }

    private static final List<String> NULL_VALUES = Arrays.asList("null", "NULL", "Null", "~");

}

