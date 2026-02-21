// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FlowDocumentation extends YAMLPsiElement {

    /**
     * Get flow description (text before in:/out: sections)
     * @return description text or null if no description
     */
    @Nullable
    String getDescription();

    /**
     * Get all input parameters from in: section
     * @return list of input parameters (empty if no in: section)
     */
    @NotNull
    List<FlowDocParameter> getInputParameters();

    /**
     * Get all output parameters from out: section
     * @return list of output parameters (empty if no out: section)
     */
    @NotNull
    List<FlowDocParameter> getOutputParameters();

    /**
     * Get the YAMLKeyValue element this documentation describes.
     * This is the next sibling element after this documentation block.
     *
     * @return the flow key-value pair or null if not found
     */
    @Nullable
    YAMLKeyValue getDocumentedFlow();

    /**
     * Get the name of the flow this documentation describes
     * @return flow name or null if not found
     */
    @Nullable
    String getFlowName();

    /**
     * Find parameter by name in either in: or out: section
     * @param parameterName parameter name to find
     * @return parameter or null if not found
     */
    @Nullable
    FlowDocParameter findParameter(@NotNull String parameterName);

    /**
     * Add input parameter to the in: section.
     * Creates the in: section if it doesn't exist.
     *
     * @param name parameter name
     * @param type parameter type (e.g. string, boolean)
     */
    void addInputParameter(@NotNull String name, @NotNull String type);
}
