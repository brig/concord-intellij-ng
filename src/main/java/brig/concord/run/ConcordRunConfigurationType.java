// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.run;

import brig.concord.ConcordBundle;
import brig.concord.ConcordIcons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class ConcordRunConfigurationType implements ConfigurationType {

    public static final String ID = "ConcordRunConfiguration";

    public static @NotNull ConcordRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(ConcordRunConfigurationType.class);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return ID;
    }

    @Override
    public @NotNull String getDisplayName() {
        return ConcordBundle.message("run.configuration.name");
    }

    @Override
    public @NotNull String getConfigurationTypeDescription() {
        return ConcordBundle.message("run.configuration.description");
    }

    @Override
    public @NotNull Icon getIcon() {
        return ConcordIcons.RUN;
    }

    @Override
    public ConfigurationFactory @NotNull [] getConfigurationFactories() {
        return new ConfigurationFactory[]{new ConcordConfigurationFactory(this)};
    }

    private static class ConcordConfigurationFactory extends ConfigurationFactory {

        protected ConcordConfigurationFactory(@NotNull ConfigurationType type) {
            super(type);
        }

        @Override
        public @NotNull @NonNls String getId() {
            return ConcordRunConfigurationType.ID;
        }

        @Override
        public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            return new ConcordRunConfiguration(project, this, ConcordBundle.message("run.configuration.name"));
        }

        @Override
        public @Nullable Class<? extends BaseState> getOptionsClass() {
            return ConcordRunConfigurationOptions.class;
        }
    }
}
