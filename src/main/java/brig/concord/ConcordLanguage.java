package brig.concord;

import com.intellij.lang.Language;
import org.jetbrains.yaml.YAMLLanguage;

public class ConcordLanguage extends Language {

    public static final ConcordLanguage INSTANCE = new ConcordLanguage();

    protected ConcordLanguage() {
        super(YAMLLanguage.INSTANCE, "Concord");
    }
}