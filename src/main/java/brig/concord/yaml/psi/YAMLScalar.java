package brig.concord.yaml.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface YAMLScalar extends YAMLValue, PsiLanguageInjectionHost {

    @NotNull
    String getTextValue();

    boolean isMultiline();

    @NotNull
    List<TextRange> getContentRanges();
}
