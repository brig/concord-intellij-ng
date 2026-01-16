package brig.concord.completion.provider;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.AnyOfType;
import brig.concord.meta.model.ExpressionMetaType;
import brig.concord.meta.model.call.*;
import brig.concord.psi.FlowDocumentation;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.psi.ref.FlowDefinitionReference;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FlowCallParamsProvider {

    private static final Key<CachedValue<FlowDocumentation>> FLOW_DOC_CACHE =
            Key.create("brig.concord.FlowCallParamsProvider.flow.documentation");

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

        var documentation = findFlowDocumentation(in);
        if (documentation == null || documentation.getInputParameters().isEmpty()) {
            return null;
        }

        var inParamName = in.getKeyText();
        for (var param : documentation.getInputParameters()) {
            if (inParamName.equals(param.getName())) {
                return param;
            }
        }
        return null;
    }

    public YamlMetaType inParams(PsiElement element) {
        if (element == null || DumbService.isDumb(element.getProject())) {
            return DEFAULT_OBJECT_TYPE;
        }

        var documentation = findFlowDocumentation(element);
        if (documentation == null || documentation.getInputParameters().isEmpty()) {
            return DEFAULT_OBJECT_TYPE;
        }

        return new FlowDocMetaType(documentation);
    }

    static class FlowDocMetaType extends ConcordMetaType implements CallInParamMetaType {

        private final FlowDocumentation documentation;
        private volatile Map<String, Supplier<YamlMetaType>> features;

        public FlowDocMetaType(FlowDocumentation documentation) {
            super("call in params");
            this.documentation = documentation;
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            var f = this.features;
            if (f != null) {
                return f;
            }

            Map<String, Supplier<YamlMetaType>> result = new HashMap<>();
            for (var param : documentation.getInputParameters()) {
                var metaType = toMetaType(param.getBaseType());
                result.put(param.getName(), () -> metaType);
            }
            this.features = Map.copyOf(result);
            return this.features;
        }

        @Override
        public @Nullable Field findFeatureByName(@NotNull String name) {
            for (var param : documentation.getInputParameters()) {
                if (name.equals(param.getName())) {
                    return metaTypeToField(toMetaType(param.getBaseType()), name);
                }
            }
            return null;
        }
    }

    public static YAMLKeyValue findCallKv(PsiElement element) {
        PsiElement prev = null;
        while (element != null && element != prev) {
            prev = element;
            var callMapping = YamlPsiUtils.getParentOfType(element, YAMLMapping.class, false);
            if (callMapping == null) return null;

            var callKv = callMapping.getKeyValueByKey("call");
            if (callKv != null) return callKv;

            element = callMapping;
        }
        return null;
    }

    private static YamlMetaType toMetaType(String type) {
        if (type == null) {
            return AnyInParamMetaType.getInstance();
        }
        return switch (type.toLowerCase()) {
            case "array" -> ARRAY_OR_EXPRESSION;
            case "string" -> STRING_OR_EXPRESSION;
            case "boolean" -> BOOLEAN_OR_EXPRESSION;
            case "object" -> OBJECT_OR_EXPRESSION;
            case "number", "int", "integer" -> NUMBER_OR_EXPRESSION;
            default -> AnyInParamMetaType.getInstance();
        };
    }

    private static @Nullable FlowDocumentation findFlowDocumentation(PsiElement element) {
        var callKv = findCallKv(element);
        if (callKv == null) {
            return null;
        }

        var flowName = callKv.getValue();
        if (flowName == null) {
            return null;
        }

        for (var ref : flowName.getReferences()) {
            if (ref instanceof FlowDefinitionReference fdr) {
                var definition = fdr.resolve();
                if (definition != null) {
                    return CachedValuesManager.getCachedValue(definition, FLOW_DOC_CACHE, () -> {
                        var doc = findFlowDocumentationBefore(definition);
                        var file = definition.getContainingFile();
                        return CachedValueProvider.Result.create(doc, file != null ? file : definition);
                    });
                }
            }
        }
        return null;
    }

    private static @Nullable FlowDocumentation findFlowDocumentationBefore(PsiElement flowDefinition) {
        var sibling = flowDefinition.getPrevSibling();
        while (sibling != null) {
            if (sibling instanceof FlowDocumentation doc) {
                return doc;
            }
            if (sibling.getTextLength() > 0 && !sibling.getText().isBlank()) {
                break;
            }
            sibling = sibling.getPrevSibling();
        }
        return null;
    }
}