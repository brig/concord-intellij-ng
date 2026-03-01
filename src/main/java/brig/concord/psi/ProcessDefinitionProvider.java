// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class ProcessDefinitionProvider {

    private static final ProcessDefinitionProvider INSTANCE = new ProcessDefinitionProvider();

    public static ProcessDefinitionProvider getInstance() {
        return INSTANCE;
    }

    public @NotNull ProcessDefinition get(@NotNull PsiElement element) {
        return new ProcessDefinition(element);
    }
}
