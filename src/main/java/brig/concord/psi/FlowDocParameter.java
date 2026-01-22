package brig.concord.psi;

import brig.concord.yaml.psi.YAMLPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FlowDocParameter extends YAMLPsiElement, PsiNamedElement {

    /**
     * Get parameter name
     * @return parameter name (e.g., "s3Bucket")
     */
    @Override
    @NotNull
    String getName();

    /**
     * Set parameter type
     * @param type new type (e.g., "string")
     * @return the updated element
     * @throws IncorrectOperationException if modification fails
     */
    PsiElement setType(@NotNull String type) throws IncorrectOperationException;

    /**
     * Get parameter type
     * @return type (e.g., "string", "int[]", "object")
     */
    @NotNull
    String getType();

    /**
     * Check if type is an array
     * @return true if type ends with []
     */
    boolean isArrayType();

    /**
     * Get base type (without [] suffix)
     * @return base type (e.g., "string" from "string[]")
     */
    @NotNull
    String getBaseType();

    /**
     * Check if parameter is mandatory
     * @return true if marked as mandatory, false if optional
     */
    boolean isMandatory();

    /**
     * Get parameter description
     * @return description text or null if no description
     */
    @Nullable
    String getDescription();

    /**
     * Check if this parameter is in the in: section (input parameter)
     * @return true if input parameter
     */
    boolean isInputParameter();

    /**
     * Check if this parameter is in the out: section (output parameter)
     * @return true if output parameter
     */
    boolean isOutputParameter();

    /**
     * Get reference to where this parameter is used in the flow code.
     * Enables Ctrl+Click navigation from documentation to usage.
     *
     * @return reference to parameter usage or null
     */
    @Nullable
    @Override
    PsiReference getReference();

    /**
     * Get all references to this parameter in the flow code.
     * Used by Find Usages.
     *
     * @return array of references
     */
    @NotNull
    @Override
    PsiReference @NotNull [] getReferences();
}