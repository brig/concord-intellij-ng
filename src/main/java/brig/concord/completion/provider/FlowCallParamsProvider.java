package brig.concord.completion.provider;

import brig.concord.documentation.FlowDefinitionDocumentationParser;
import brig.concord.documentation.FlowDocumentation;
import brig.concord.documentation.ParamType;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.AnyOfType;
import brig.concord.meta.model.ExpressionMetaType;
import brig.concord.meta.model.call.*;
import brig.concord.psi.CommentsProcessor;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.psi.ref.FlowDefinitionReference;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;

import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;

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

        var documentation = flowDocumentation(in);
        if (documentation == null || documentation.in().list().isEmpty()) {
            return null;
        }


        var inParamName = in.getKeyText();
        var paramDef = documentation.in().find(inParamName);
        if (paramDef == null) {
            return null;
        }

        return paramDef.element();
    }

    public YamlMetaType inParams(PsiElement element) {
        if (element == null || DumbService.isDumb(element.getProject())) {
            return DEFAULT_OBJECT_TYPE;
        }

        var documentation = flowDocumentation(element);
        if (documentation == null || documentation.in().list().isEmpty()) {
            return DEFAULT_OBJECT_TYPE;
        }

        return new MT(documentation);
    }

    static class MT extends ConcordMetaType implements CallInParamMetaType {

        private final FlowDocumentation documentation;
        private volatile Map<String, Supplier<YamlMetaType>> features;

        public MT(FlowDocumentation documentation) {
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
            for (var p : documentation.in().list()) {
                var metaType = toMetaType(p.type());
                result.put(p.name(), () -> metaType);
            }
            this.features = Map.copyOf(result);
            return this.features;
        }

        @Override
        public @Nullable Field findFeatureByName(@NotNull String name) {
            var def = documentation.in().find(name);
            if (def == null) {
                return null;
            }

            return metaTypeToField(toMetaType(def.type()), name);
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
        var callKv = findCallKv(element);
        if (callKv == null) {
            return null;
        }

        var flowName = callKv.getValue();
        if (flowName == null) {
            return null;
        }

        var flowRefs = flowName.getReferences();
        for (var ref : flowRefs) {
            if (ref instanceof FlowDefinitionReference fdr) {
                var definition = fdr.resolve();
                if (definition != null) {
                    return CachedValuesManager.getCachedValue(definition, FLOW_DOC_CACHE, () -> {
                        var start = CommentsProcessor.findFirst(definition.getPrevSibling());
                        var doc = FlowDefinitionDocumentationParser.parse(start);

                        var file = definition.getContainingFile();
                        if (file != null) {
                            return CachedValueProvider.Result.create(doc, file);
                        }

                        return CachedValueProvider.Result.create(doc, PsiModificationTracker.MODIFICATION_COUNT, definition);
                    });
                }
            }
        }
        return null;
    }
}
