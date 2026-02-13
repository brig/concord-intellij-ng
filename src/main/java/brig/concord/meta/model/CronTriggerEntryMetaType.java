package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.meta.model.value.TimezoneMetaType;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class CronTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final CronTriggerEntryMetaType INSTANCE = new CronTriggerEntryMetaType();

    public static CronTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "spec", new StringMetaType("cron", desc("doc.triggers.cron.spec.description").andRequired()),
            "entryPoint", new CallMetaType(desc("doc.triggers.cron.entryPoint.description").andRequired()),
            "runAs", new RunAsMetaType(desc("doc.triggers.cron.runAs.description")),
            "activeProfiles", new StringArrayMetaType(desc("doc.triggers.cron.activeProfiles.description")),
            "timezone", TimezoneMetaType.getInstance(),
            "arguments", new AnyMapMetaType(desc("doc.triggers.cron.arguments.description")),
            "exclusive", TriggerExclusiveMetaType.getInstance()
    );

    private CronTriggerEntryMetaType() {
        super(desc("doc.triggers.cron.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class RunAsMetaType extends ConcordMetaType {

        private static final Map<String, YamlMetaType> features = Map.of(
                "withSecret", StringMetaType.getInstance()
        );

        private RunAsMetaType(@NotNull TypeProps props) {
            super(props);
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }
    }
}
