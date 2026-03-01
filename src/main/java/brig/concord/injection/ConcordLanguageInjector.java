// SPDX-License-Identifier: Apache-2.0
package brig.concord.injection;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.ScriptStepMetaType;
import brig.concord.meta.model.StepElementMetaType;
import brig.concord.meta.model.StepMetaMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcordLanguageInjector implements MultiHostInjector {

    private final Map<String, String> prefixes = new ConcurrentHashMap<>();

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!(context instanceof YAMLScalar scalar)) {
            return;
        }

        if (!scalar.isValidHost()) {
            return;
        }

        var parent = scalar.getParent();
        if (!(parent instanceof YAMLKeyValue kv)) {
            return;
        }

        if (!ScriptStepMetaType.BODY_KEY.equals(kv.getKeyText())) {
            return;
        }

        var grandParent = kv.getParent();
        if (!(grandParent instanceof YAMLMapping mapping)) {
            return;
        }

        var scriptKv = mapping.getKeyValueByKey(ScriptStepMetaType.SCRIPT_KEY);
        if (scriptKv == null) {
            return;
        }

        var typeProxy = ConcordMetaTypeProvider.getInstance(context.getProject()).getMetaTypeProxy(scriptKv);
        if (typeProxy != null && !(typeProxy.getMetaType() instanceof StepElementMetaType)) {
            return;
        }

        var scriptType = scriptKv.getValueText();
        var language = getLanguage(scriptType);

        if (language != null) {
            var ranges = scalar.getContentRanges();
            if (ranges.isEmpty()) {
                return;
            }

            var start = ranges.getFirst().getStartOffset();
            var end = ranges.getLast().getEndOffset();
            var commonRange = TextRange.create(start, end);

            registrar.startInjecting(language);

            var prefix = getPrefix(language);
            registrar.addPlace(prefix, null, (PsiLanguageInjectionHost) context, commonRange);

            registrar.doneInjecting();
        }
    }

    private @Nullable String getPrefix(Language language) {
        var id = language.getID();
        var cached = prefixes.get(id);
        if (cached != null) {
            return cached;
        }
        var loaded = loadPrefix(id);
        if (loaded != null) {
            prefixes.put(id, loaded);
        }
        return loaded;
    }

    private String loadPrefix(String langId) {
        var resourceName = switch (langId) {
            case "Groovy" -> "/injection/groovy-prefix.groovy";
            case "JavaScript" -> "/injection/javascript-prefix.js";
            case "Python" -> "/injection/python-prefix.py";
            case "Ruby" -> "/injection/ruby-prefix.rb";
            default -> null;
        };

        if (resourceName != null) {
            try (var stream = getClass().getResourceAsStream(resourceName)) {
                if (stream != null) {
                    var content = new String(FileUtil.loadBytes(stream), StandardCharsets.UTF_8);
                    return StringUtil.convertLineSeparators(content);
                }
            } catch (IOException e) {
                // Ignore
            }
        }
        return null;
    }

    private Language getLanguage(String scriptType) {
        if (scriptType == null) {
            return null;
        }

        return switch (scriptType.trim().toLowerCase()) {
            case "groovy" -> Language.findLanguageByID("Groovy");
            case "js", "javascript" -> Language.findLanguageByID("JavaScript");
            case "python" -> Language.findLanguageByID("Python");
            case "ruby" -> Language.findLanguageByID("Ruby");
            default -> null;
        };
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(YAMLScalar.class);
    }
}
