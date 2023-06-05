package brig.concord.documentation;

import com.intellij.psi.PsiComment;

import java.util.Objects;

public final class ParamDocumentation {
    private final PsiComment element;
    private final String name;
    private final ParamType type;
    private final boolean mandatory;

    public ParamDocumentation(PsiComment element, String name, ParamType type, boolean mandatory) {
        this.element = element;
        this.name = name;
        this.type = type;
        this.mandatory = mandatory;
    }

    public PsiComment element() {
        return element;
    }

    public String name() {
        return name;
    }

    public ParamType type() {
        return type;
    }

    public boolean mandatory() {
        return mandatory;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParamDocumentation) obj;
        return Objects.equals(this.element, that.element) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type) &&
                this.mandatory == that.mandatory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, name, type, mandatory);
    }

    @Override
    public String toString() {
        return "ParamDocumentation[" +
                "element=" + element + ", " +
                "name=" + name + ", " +
                "type=" + type + ", " +
                "mandatory=" + mandatory + ']';
    }

}
