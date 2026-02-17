// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import brig.concord.ConcordLanguage;

public class YAMLCodeStyleSettings extends CustomCodeStyleSettings {
    public int ALIGN_VALUES_PROPERTIES = DO_NOT_ALIGN;

    public static final int DO_NOT_ALIGN = 0;
    public static final int ALIGN_ON_VALUE = 1;
    public static final int ALIGN_ON_COLON = 2;

    public boolean INDENT_SEQUENCE_VALUE = true;

    public boolean SEQUENCE_ON_NEW_LINE = false;
    public boolean BLOCK_MAPPING_ON_NEW_LINE = false;

    public boolean SPACE_BEFORE_COLON = false;

    /** Whether editor should automatically insert hyphen on Enter for subsequent (non-first) items */
    public boolean AUTOINSERT_SEQUENCE_MARKER = true;

    public YAMLCodeStyleSettings(CodeStyleSettings container) {
        super(ConcordLanguage.INSTANCE.getID(), container);
    }
}
