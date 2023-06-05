package brig.concord.documentation;

import com.intellij.psi.PsiComment;

public record ParamDocumentation(PsiComment element, String name, ParamType type, boolean mandatory) {
}
