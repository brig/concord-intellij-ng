/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 Concord Plugin Authors
 */
package brig.concord.el;

import com.intellij.lang.Language;

public class ElLanguage extends Language {

    public static final ElLanguage INSTANCE = new ElLanguage();

    private ElLanguage() {
        super("concord-el");
    }
}
