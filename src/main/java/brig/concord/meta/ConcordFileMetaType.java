package brig.concord.meta;

import brig.concord.meta.model.ConfigurationMetaType;
import brig.concord.meta.model.FlowsMetaType;
import brig.concord.meta.model.ImportsMetaType;
import brig.concord.meta.model.TriggersMetaType;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConcordFileMetaType extends ConcordMetaType {

    private static final ConcordFileMetaType INSTANCE = new ConcordFileMetaType();

    public static ConcordFileMetaType getInstance() {
        return INSTANCE;
    }

    protected ConcordFileMetaType() {
        super("ConcordFile");
    }

    protected static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("resources", ConcordAnyMapMetaType::getInstance); // TODO: types
        features.put("configuration", ConfigurationMetaType::getInstance);
        features.put("publicFlows", ConcordStringArrayMetaType::getInstance);
        features.put("forms", ConcordAnyMapMetaType::getInstance); // TODO: types
        features.put("imports", ImportsMetaType::getInstance); // TODO: types
        features.put("profiles", ConcordAnyMapMetaType::getInstance); // TODO: types
        features.put("flows", FlowsMetaType::getInstance);
        features.put("triggers", TriggersMetaType::getInstance); // TODO: types
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    private String getFilename(PsiElement element) {
        return element.getContainingFile().getName();
    }
}
