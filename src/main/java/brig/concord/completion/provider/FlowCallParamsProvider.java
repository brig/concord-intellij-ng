package brig.concord.completion.provider;

import brig.concord.documentation.FlowDefinitionDocumentationParser;
import brig.concord.documentation.FlowDocumentation;
import brig.concord.documentation.ParamDocumentation;
import brig.concord.documentation.ParamType;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.AnyOfType;
import brig.concord.meta.model.ExpressionMetaType;
import brig.concord.meta.model.call.*;
import brig.concord.psi.CommentsProcessor;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.psi.ref.FlowDefinitionReference;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class FlowCallParamsProvider {

    public static final AnyOfType ARRAY_OR_EXPRESSION = AnyOfType.anyOf(AnyArrayInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType BOOLEAN_OR_EXPRESSION = AnyOfType.anyOf(BooleanInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType OBJECT_OR_EXPRESSION = AnyOfType.anyOf(AnyMapInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType NUMBER_OR_EXPRESSION = AnyOfType.anyOf(IntegerInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType STRING_OR_EXPRESSION = AnyOfType.anyOf(StringInParamMetaType.getInstance(), ExpressionMetaType.getInstance());

    private static final YamlMetaType DEFAULT_OBJECT_TYPE = AnyMapInParamMetaType.getInstance();

    private static final FlowCallParamsProvider INSTANCE = new FlowCallParamsProvider();

    public static FlowCallParamsProvider getInstance() {
        return INSTANCE;
    }

    public PsiElement inParamDefinition(YAMLKeyValue in) {
        if (in == null || DumbService.isDumb(in.getProject())) {
            return null;
        }

        FlowDocumentation documentation = flowDocumentation(in);
        if (documentation == null || documentation.in().list().isEmpty()) {
            return null;
        }


        String inParamName = in.getKeyText();
        ParamDocumentation paramDef = documentation.in().find(inParamName);
        if (paramDef == null) {
            return null;
        }

        return paramDef.element();
    }

    public YamlMetaType inParams(PsiElement element) {
        if (element == null || DumbService.isDumb(element.getProject())) {
            return DEFAULT_OBJECT_TYPE;
        }

        FlowDocumentation documentation = flowDocumentation(element);
        if (documentation == null || documentation.in().list().isEmpty()) {
            return DEFAULT_OBJECT_TYPE;
        }

        return new MT(documentation);
    }

    static class MT extends ConcordMetaType implements CallInParamMetaType {

        private final FlowDocumentation documentation;

        public MT(FlowDocumentation documentation) {
            super("call in params");

            this.documentation = documentation;
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            Map<String, Supplier<YamlMetaType>> result = new HashMap<>();
            for (ParamDocumentation p : documentation.in().list()) {
                YamlMetaType metaType = toMetaType(p.type());
                result.put(p.name(), () -> metaType);
            }

            return result;
        }

        @Override
        public @Nullable Field findFeatureByName(@NotNull String name) {
            ParamDocumentation def = documentation.in().find(name);
            if (def == null) {
                return null;
            }

            return metaTypeToField(toMetaType(def.type()), name);
        }
    }

    public static YAMLKeyValue findCallKv(PsiElement element) {
        while (true) {
            YAMLMapping callMapping = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, false);
            if (callMapping == null) {
                return null;
            }

            YAMLKeyValue callKv = callMapping.getKeyValueByKey("call");
            if (callKv != null) {
                return callKv;
            }

            element = callMapping;
        }
    }

    private static YamlMetaType toMetaType(ParamType type) {
        switch (type) {
            case ARRAY -> {
                return ARRAY_OR_EXPRESSION;
            }
            case STRING -> {
                return STRING_OR_EXPRESSION;
            }
            case BOOLEAN -> {
                return BOOLEAN_OR_EXPRESSION;
            }
            case OBJECT -> {
                return OBJECT_OR_EXPRESSION;
            }
            case NUMBER -> {
                return NUMBER_OR_EXPRESSION;
            }
            default -> {
                return AnyInParamMetaType.getInstance();
            }
        }
    }

    private static FlowDocumentation flowDocumentation(PsiElement element) {
        YAMLKeyValue callKv = findCallKv(element);
        if (callKv == null) {
            return null;
        }

        YAMLValue flowName = callKv.getValue();
        if (flowName == null) {
            return null;
        }

        PsiReference [] flowRefs = flowName.getReferences();
        for (PsiReference ref : flowRefs) {
            if (ref instanceof FlowDefinitionReference fdr) {
                PsiElement definition = fdr.resolve();
                if (definition != null) {
                    PsiComment start = CommentsProcessor.findFirst(definition.getPrevSibling());
                    return FlowDefinitionDocumentationParser.parse(start);
                }
            }
        }
        return null;
    }
}
