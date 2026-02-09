package brig.concord.injection;

import brig.concord.highlighting.HighlightingTestBase;
import com.intellij.lang.Language;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConcordLanguageInjectorTest extends HighlightingTestBase {

    @Test
    void testJsInjection() {
        configureFromText("""
            flows:
              default:
                - script: js
                  body: |
                    if (isDryRun) {
                      log.info("DRY RUN")
                      return
                    }
                    result.set("myVar", "myValue")
            """);

        var scalar = value("/flows/default[0]/body").element();

        var language = Language.findLanguageByID("JavaScript");
        Assertions.assertNotNull(language, "Language not available in test environment, skipping assertion.");

        InjectedLanguageManager manager = InjectedLanguageManager.getInstance(myFixture.getProject());
        ReadAction.run(() -> {
            List<Pair<PsiElement, TextRange>> injected = manager.getInjectedPsiFiles(scalar);

            Assertions.assertNotNull(injected, "Should have injected language");
            Assertions.assertFalse(injected.isEmpty(), "Should have injected language");
            Assertions.assertEquals("JavaScript", injected.getFirst().getFirst().getLanguage().getID());
        });
        myFixture.checkHighlighting(true, false, true);
    }

    @Test
    void testGroovyInjection() {
        configureFromText("""
            flows:
              default:
              - script: groovy
                body: |
                  if (isDryRun) {
                     log.info("DRY RUN")
                     return
                  }
                  result.set("myVar", "myValue")
                out: scriptResult
            """);

        var scalar = value("/flows/default[0]/body").element();

        var language = Language.findLanguageByID("Groovy");
        Assertions.assertNotNull(language, "Language not available in test environment, skipping assertion.");

        InjectedLanguageManager manager = InjectedLanguageManager.getInstance(myFixture.getProject());
        ReadAction.run(() -> {
            List<Pair<PsiElement, TextRange>> injected = manager.getInjectedPsiFiles(scalar);

            Assertions.assertNotNull(injected, "Should have injected language");
            Assertions.assertFalse(injected.isEmpty(), "Should have injected language");
            Assertions.assertEquals("Groovy", injected.getFirst().getFirst().getLanguage().getID());
        });

        myFixture.checkHighlighting(true, false, true);
    }
}
