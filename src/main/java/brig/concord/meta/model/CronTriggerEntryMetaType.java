package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.meta.model.value.TimezoneMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class CronTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final CronTriggerEntryMetaType INSTANCE = new CronTriggerEntryMetaType();

    public static CronTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("spec", "entryPoint");

    private static final Map<String, YamlMetaType> features = Map.of(
            "spec", new StringMetaType("cron").withDescriptionKey("doc.triggers.cron.spec.description"),
            "entryPoint", new CallMetaType().withDescriptionKey("doc.triggers.cron.entryPoint.description"),
            "runAs", new RunAsMetaType().withDescriptionKey("doc.triggers.cron.runAs.description"),
            "activeProfiles", new StringArrayMetaType().withDescriptionKey("doc.triggers.cron.activeProfiles.description"),
            "timezone", TimezoneMetaType.getInstance(),
            "arguments", new AnyMapMetaType().withDescriptionKey("doc.triggers.cron.arguments.description"),
            "exclusive", TriggerExclusiveMetaType.getInstance()
    );

    private CronTriggerEntryMetaType() {
        setDescriptionKey("doc.triggers.cron.description");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class RunAsMetaType extends ConcordMetaType {

        private static final RunAsMetaType INSTANCE = new RunAsMetaType();

        public static RunAsMetaType getInstance() {
            return INSTANCE;
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "withSecret", StringMetaType.getInstance()
        );

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }
    }
}
