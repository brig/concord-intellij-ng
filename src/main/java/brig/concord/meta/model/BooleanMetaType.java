package brig.concord.meta.model;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.yaml.meta.model.YamlBooleanType;

@SuppressWarnings("UnstableApiUsage")
public class BooleanMetaType extends YamlBooleanType {

    private static final BooleanMetaType INSTANCE = new BooleanMetaType();

    public static BooleanMetaType getInstance() {
        return INSTANCE;
    }

    public BooleanMetaType() {
        super("yaml:boolean");
        setDisplayName("boolean");
        withLiterals("true", "false");

        withHiddenLiterals(new LiteralBuilder()
                .withLiteral("true", StringUtil::toUpperCase, StringUtil::toTitleCase)
                .withLiteral("false", StringUtil::toUpperCase, StringUtil::toTitleCase)
                .withAllCasesOf("on", "off", "yes", "no")
                .toArray());
    }
}
