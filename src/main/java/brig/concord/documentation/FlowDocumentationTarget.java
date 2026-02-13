package brig.concord.documentation;

import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class FlowDocumentationTarget implements DocumentationTarget {

    private final SmartPsiElementPointer<YAMLKeyValue> callPointer;
    private final String flowName;
    private final @Nullable String description;
    private final List<ParamInfo> inputParams;
    private final List<ParamInfo> outputParams;

    public FlowDocumentationTarget(@NotNull YAMLKeyValue callKv, @NotNull FlowDocumentation flowDoc) {
        this.callPointer = SmartPointerManager.createPointer(callKv);
        this.flowName = flowDoc.getFlowName();
        this.description = flowDoc.getDescription();
        this.inputParams = toParamInfos(flowDoc.getInputParameters());
        this.outputParams = toParamInfos(flowDoc.getOutputParameters());
    }

    private FlowDocumentationTarget(@NotNull YAMLKeyValue callKv,
                                    @Nullable String flowName,
                                    @Nullable String description,
                                    @NotNull List<ParamInfo> inputParams,
                                    @NotNull List<ParamInfo> outputParams) {
        this.callPointer = SmartPointerManager.createPointer(callKv);
        this.flowName = flowName;
        this.description = description;
        this.inputParams = inputParams;
        this.outputParams = outputParams;
    }

    @Override
    public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
        var ptr = this.callPointer;
        var name = this.flowName;
        var desc = this.description;
        var inParams = this.inputParams;
        var outParams = this.outputParams;
        return () -> {
            var element = ptr.getElement();
            return element == null ? null : new FlowDocumentationTarget(element, name, desc, inParams, outParams);
        };
    }

    @Override
    public @NotNull TargetPresentation computePresentation() {
        return TargetPresentation.builder(flowName != null ? flowName : "flow").presentation();
    }

    @Override
    public @Nullable String computeDocumentationHint() {
        return description;
    }

    @Override
    public @Nullable DocumentationResult computeDocumentation() {
        var element = callPointer.getElement();
        if (element == null) {
            return null;
        }

        var sb = new StringBuilder();

        sb.append(DEFINITION_START)
                .append(StringUtil.escapeXmlEntities(flowName != null ? flowName : "flow"))
                .append(DEFINITION_END);

        sb.append(CONTENT_START);

        if (description != null) {
            sb.append("<p>").append(StringUtil.decapitalize(description)).append("</p>");
        }

        appendParamSection(sb, "Input Parameters:", inputParams);
        appendParamSection(sb, "Output Parameters:", outputParams);

        sb.append(CONTENT_END);

        return DocumentationResult.documentation(sb.toString());
    }

    private static void appendParamSection(StringBuilder sb, String title, List<ParamInfo> params) {
        if (params.isEmpty()) {
            return;
        }

        sb.append("<p><b>").append(title).append("</b></p>");
        sb.append("<ul>");
        for (var param : params) {
            sb.append("<li><code>").append(StringUtil.escapeXmlEntities(param.name)).append("</code>");
            if (param.type != null) {
                sb.append(" <i>(").append(StringUtil.escapeXmlEntities(param.type));
                if (param.mandatory) {
                    sb.append(", required");
                }
                sb.append(")</i>");
            }
            if (param.description != null) {
                sb.append(" &mdash; ").append(StringUtil.decapitalize(param.description));
            }
            sb.append("</li>");
        }
        sb.append("</ul>");
    }

    private static List<ParamInfo> toParamInfos(List<FlowDocParameter> params) {
        return params.stream()
                .map(p -> new ParamInfo(p.getName(), p.getType(), p.isMandatory(), p.getDescription()))
                .toList();
    }

    private record ParamInfo(@NotNull String name, @Nullable String type, boolean mandatory,
                     @Nullable String description) {
    }
}
