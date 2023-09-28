package brig.concord.documentation;

import brig.concord.psi.CommentsProcessor;
import com.google.common.base.CharMatcher;
import com.intellij.psi.PsiComment;

import java.util.LinkedList;
import java.util.List;

public class FlowDefinitionDocumentationParser {

    enum State {
        INIT,
        DESCRIPTION,
        IN,
        OUT
    }

    public static boolean isHeader(String line) {
        return line.trim().startsWith("##");
    }

    public static boolean isIn(String cleanLine) {
        return cleanLine.toLowerCase().startsWith("in:");
    }

    public static boolean isOut(String cleanLine) {
        return cleanLine.toLowerCase().startsWith("out:");
    }

    public static FlowDocumentation parse(PsiComment start) {
        State state = State.INIT;
        StringBuilder description = new StringBuilder();
        List<ParamDocumentation> in = new LinkedList<>();
        List<ParamDocumentation> out = new LinkedList<>();

        for (PsiComment comment : CommentsProcessor.comments(start)) {
            String line = comment.getText();
            if (state == State.INIT) {
                if (isHeader(line)) {
                    state = State.DESCRIPTION;
                    continue;
                } else {
                    break;
                }
            }

            String clean = CharMatcher.anyOf("#").trimLeadingFrom(line).trim();
            if (isIn(clean) && state != State.IN) {
                state = State.IN;
                continue;
            } else if (isOut(clean) && state != State.OUT) {
                state = State.OUT;
                continue;
            }

            switch (state) {
                case IN ->  {
                    ParamDocumentation param = parseParam(comment, clean);
                    if (param != null) {
                        in.add(param);
                    }
                }
                case OUT -> {
                    ParamDocumentation param = parseParam(comment, clean);
                    if (param != null) {
                        out.add(param);
                    }
                }
                case DESCRIPTION -> {
                    if (!clean.isEmpty()) {
                        description.append(clean);
                    }
                }
            }
        }
        return new FlowDocumentation(description.toString(), in, out);
    }

    private static ParamDocumentation parseParam(PsiComment element, String str) {
        int pos = str.indexOf(":");
        if (pos < 0) {
            return null;
        }

        String name = str.substring(0, pos);
        // TODO: parse keys
        if (name.contains(".")) {
            return null;
        }
        ParamType paramType = ParamType.ANY;
        int pos2 = str.indexOf(",", pos);
        if (pos2 > 0) {
            String type = str.substring(pos + 1, pos2);
            paramType = toParamType(type);
        }
        return new ParamDocumentation(element, name, paramType, false);
    }

    private static ParamType toParamType(String type) {
        try {
            return ParamType.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            return ParamType.ANY;
        }
    }
}
