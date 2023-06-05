package brig.concord.documentation;

import java.util.List;

public record FlowDocumentation(String description,
                                List<ParamDocumentation> in,
                                List<ParamDocumentation> out) {
}
