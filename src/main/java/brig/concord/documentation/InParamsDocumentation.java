package brig.concord.documentation;

import java.util.List;
import java.util.stream.Collectors;

public class InParamsDocumentation {

    private static final String ANY_PARAM = "<any>";

    private final List<ParamDocumentation> params;

    private final ParamDocumentation anyParam;

    public InParamsDocumentation(List<ParamDocumentation> params) {
        this.params = params.stream().filter(p -> !ANY_PARAM.equals(p.name())).collect(Collectors.toList());
        this.anyParam = params.stream().filter(p -> ANY_PARAM.equals(p.name())).findAny().orElse(null);
    }

    public ParamDocumentation find(String name) {
        return params.stream()
                .filter(p -> p.name().equals(name))
                .findAny()
                .orElse(anyParam);
    }

    public List<ParamDocumentation> list() {
        return params;
    }
}
