package brig.concord.documentation;

import java.util.List;
import java.util.Objects;

public final class FlowDocumentation {
    private final String description;
    private final InParamsDocumentation in;
    private final List<ParamDocumentation> out;

    public FlowDocumentation(String description,
                             List<ParamDocumentation> in,
                             List<ParamDocumentation> out) {
        this.description = description;
        this.in = new InParamsDocumentation(in);
        this.out = out;
    }

    public String description() {
        return description;
    }

    public InParamsDocumentation in() {
        return in;
    }

    public List<ParamDocumentation> out() {
        return out;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FlowDocumentation) obj;
        return Objects.equals(this.description, that.description) &&
                Objects.equals(this.in, that.in) &&
                Objects.equals(this.out, that.out);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, in, out);
    }

    @Override
    public String toString() {
        return "FlowDocumentation[" +
                "description=" + description + ", " +
                "in=" + in + ", " +
                "out=" + out + ']';
    }

}
