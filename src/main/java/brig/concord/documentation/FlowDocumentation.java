package brig.concord.documentation;

import java.util.List;

public record FlowDocumentation(String description, InParamsDocumentation in, List<ParamDocumentation> out) {

    public FlowDocumentation(String description,
                             List<ParamDocumentation> in,
                             List<ParamDocumentation> out) {
        this(description, new InParamsDocumentation(in), out);
    }
}
