// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.smart;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
        name = "YamlEditorOptions",
        storages = @Storage("editor.xml"),
        category = SettingsCategory.CODE
)
public class YAMLEditorOptions implements PersistentStateComponent<YAMLEditorOptions> {
    private boolean myUseSmartPaste = true;

    @Override
    public @NotNull YAMLEditorOptions getState() {
        return this;
    }

    @Override
    public void loadState(final @NotNull YAMLEditorOptions state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void noStateLoaded() {
        loadState(new YAMLEditorOptions());
    }

    public boolean isUseSmartPaste() {
        return myUseSmartPaste;
    }

    public void setUseSmartPaste(boolean useSmartPaste) {
        this.myUseSmartPaste = useSmartPaste;
    }

    public static @NotNull YAMLEditorOptions getInstance() {
        return ApplicationManager.getApplication().getService(YAMLEditorOptions.class);
    }
}
