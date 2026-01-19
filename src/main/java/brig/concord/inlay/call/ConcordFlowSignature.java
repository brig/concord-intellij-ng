package brig.concord.inlay.call;

import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ConcordFlowSignature {
    private final @Nullable String flowName;
    private final @NotNull List<FlowDocParameter> inParams;
    private final @NotNull List<FlowDocParameter> outParams;

    public ConcordFlowSignature(
            @Nullable String flowName,
            @NotNull List<FlowDocParameter> inParams,
            @NotNull List<FlowDocParameter> outParams
    ) {
        this.flowName = flowName;
        this.inParams = inParams;
        this.outParams = outParams;
    }

    public static @NotNull ConcordFlowSignature from(@NotNull FlowDocumentation doc) {
        return new ConcordFlowSignature(
                doc.getFlowName(),
                doc.getInputParameters(),
                doc.getOutputParameters()
        );
    }

    public String renderForPopup() {
        var sb = new StringBuilder();

        if (flowName != null && !flowName.isBlank()) {
            sb.append(flowName);
        }

        if (!inParams.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append("in: ");
            appendParams(sb, inParams);
        }

        if (!outParams.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append("out: ");
            appendParams(sb, outParams);
        }

        if (sb.isEmpty()) {
            return "no documented parameters";
        }
        return sb.toString();
    }

    private static void appendParams(@NotNull StringBuilder sb, @NotNull List<FlowDocParameter> params) {
        var first = true;
        for (var param : params) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            sb.append(param.getName());
            sb.append(": ");
            sb.append(param.getType());
            if (param.isMandatory()) {
                sb.append(" (mandatory)");
            }
        }
    }
}
