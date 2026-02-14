package brig.concord.completion.provider;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.value.*;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.meta.model.call.CallInParamMetaType;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.psi.YamlPsiUtils;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLPsiElement;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.HashMap;
import java.util.Map;

public class FlowCallParamsProvider {

    private static final Key<CachedValue<FlowDocumentation>> FLOW_DOC_CACHE =
            Key.create("brig.concord.FlowCallParamsProvider.flow.documentation");

    private static final Key<CachedValue<FlowDocumentation>> CALL_SITE_DOC_CACHE =
            Key.create("brig.concord.FlowCallParamsProvider.call.site.doc");

    private static final Key<CachedValue<FlowDocMetaType>> FLOW_DOC_META_TYPE_CACHE =
            Key.create("brig.concord.FlowCallParamsProvider.flow.doc.meta.type");

    private static final YamlMetaType DEFAULT_OBJECT_TYPE = AnyMapMetaType.getInstance();

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

        return CachedValuesManager.getCachedValue(documentation, FLOW_DOC_META_TYPE_CACHE, () ->
                CachedValueProvider.Result.create(new FlowDocMetaType(documentation), documentation));
    }

    static class FlowDocMetaType extends ConcordMetaType implements CallInParamMetaType {

        private final FlowDocumentation documentation;
        private volatile Map<String, YamlMetaType> features;

        public FlowDocMetaType(FlowDocumentation documentation) {
            this.documentation = documentation;
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            var f = this.features;
            if (f != null) {
                return f;
            }

            var result = new HashMap<String, YamlMetaType>();
            for (var param : documentation.getInputParameters()) {
                var metaType = toMetaType(param);
                result.put(param.getName(), metaType);
            }
            this.features = Map.copyOf(result);
            return this.features;
        }

        @Override
        public @Nullable Field findFeatureByName(@NotNull String name) {
            for (var param : documentation.getInputParameters()) {
                if (name.equals(param.getName())) {
                    return metaTypeToField(toMetaType(param), name);
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

    private static YamlMetaType toMetaType(FlowDocParameter parameter) {
        if (parameter == null) {
            return AnythingMetaType.getInstance();
        }

        AnyOfType baseType;
        if (parameter.isArrayType()) {
            baseType = switch (parameter.getBaseType().toLowerCase()) {
                case "string" -> ParamMetaTypes.STRING_ARRAY_OR_EXPRESSION;
                case "boolean" -> ParamMetaTypes.BOOLEAN_ARRAY_OR_EXPRESSION;
                case "object" -> ParamMetaTypes.OBJECT_ARRAY_OR_EXPRESSION;
                case "number", "int", "integer" -> ParamMetaTypes.NUMBER_ARRAY_OR_EXPRESSION;
                default -> ParamMetaTypes.ARRAY_OR_EXPRESSION;
            };
        } else {
            baseType = switch (parameter.getBaseType().toLowerCase()) {
                case "string" -> ParamMetaTypes.STRING_OR_EXPRESSION;
                case "boolean" -> ParamMetaTypes.BOOLEAN_OR_EXPRESSION;
                case "object" -> ParamMetaTypes.OBJECT_OR_EXPRESSION;
                case "number", "int", "integer" -> ParamMetaTypes.NUMBER_OR_EXPRESSION;
                default -> null;
            };
        }

        if (baseType == null) {
            return AnythingMetaType.getInstance();
        }

        var description = parameter.getDescription();
        var mandatory = parameter.isMandatory();

        return baseType.withProps(TypeProps.desc(description).andRequired(mandatory));
    }

    public static @Nullable FlowDocumentation findFlowDocumentation(PsiElement element) {
        var callKv = findCallKv(element);
        if (callKv == null) {
            return null;
        }

        return CachedValuesManager.getCachedValue(callKv, CALL_SITE_DOC_CACHE, () -> {
            var doc = doFindFlowDocumentation(callKv);
            return CachedValueProvider.Result.create(doc, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    @Nullable
    private static FlowDocumentation doFindFlowDocumentation(YAMLKeyValue callKv) {
        var flowNameValue = callKv.getValue();
        if (!(flowNameValue instanceof YAMLScalar scalar)) {
            return null;
        }

        var flowName = scalar.getTextValue();
        if (flowName.isBlank()) {
            return null;
        }

        var process = ProcessDefinitionProvider.getInstance().get(callKv);
        var definition = process.flow(flowName);
        if (definition == null) {
            return null;
        }

        var doc = findFlowDocCached(definition);
        if (doc != null) {
            return doc;
        }

        // In a completion copy the original file's PSI may be broken
        // (e.g., an empty sequence item `- ` causes the parser to absorb
        // the `##` flow-doc block into the preceding key-value).
        // The completion copy itself has valid PSI (the dummy identifier fills
        // the empty slot), so look for the flow definition there.
        var file = callKv.getContainingFile();
        if (file != null && file != file.getOriginalFile()) {
            var localDef = findFlowDefinitionInFile(file, flowName);
            if (localDef != null) {
                return findFlowDocCached(localDef);
            }
        }

        return null;
    }

    private static @Nullable FlowDocumentation findFlowDocCached(PsiElement definition) {
        return CachedValuesManager.getCachedValue(definition, FLOW_DOC_CACHE, () -> {
            var doc = findFlowDocumentationBefore(definition);
            var file = definition.getContainingFile();
            return CachedValueProvider.Result.create(doc, file != null ? file : definition);
        });
    }

    /**
     * Finds a flow definition directly in the given file by PSI navigation,
     * without using the file-based index.
     */
    private static @Nullable PsiElement findFlowDefinitionInFile(PsiFile file, String flowName) {
        var doc = PsiTreeUtil.getChildOfType(file, YAMLDocument.class);
        if (doc == null) {
            return null;
        }

        var flowKey = YamlPsiUtils.get(doc, YAMLPsiElement.class, "flows", flowName);
        if (flowKey == null) {
            return null;
        }

        return flowKey.getParent();
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
