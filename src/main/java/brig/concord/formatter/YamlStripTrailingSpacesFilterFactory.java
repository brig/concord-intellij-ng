package brig.concord.formatter;

import brig.concord.ConcordLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.PsiBasedStripTrailingSpacesFilter;
import com.intellij.psi.PsiFile;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YamlRecursivePsiElementVisitor;

public class YamlStripTrailingSpacesFilterFactory extends PsiBasedStripTrailingSpacesFilter.Factory {

    @Override
    public PsiBasedStripTrailingSpacesFilter createFilter(Document document) {
        return new PsiBasedStripTrailingSpacesFilter(document) {
            @Override
            public void process(PsiFile psiFile) {
                psiFile.accept(new YamlRecursivePsiElementVisitor() {
                    @Override
                    public void visitScalar(YAMLScalar scalar) {
                        disableRange(scalar.getTextRange(), false);
                        super.visitScalar(scalar);
                    }
                });
            }
        };
    }

    @Override
    public boolean isApplicableTo(Language language) {
        return language.isKindOf(ConcordLanguage.INSTANCE);
    }
}
