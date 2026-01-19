package brig.concord.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import brig.concord.lexer.FlowDocElementTypes;
import static brig.concord.lexer.FlowDocTokenTypes.*;
import static brig.concord.yaml.YAMLTokenTypes.*;

public class YAMLParserFlowDocExtension {

    /**
     * Parse complete flow documentation block:
     * ##
     * # description
     * # in:
     * #   param: type, mandatory, description
     * # out:
     * #   param: type, mandatory, description
     * ##
     */
    public static void parseFlowDocumentation(PsiBuilder builder) {
        PsiBuilder.Marker docMarker = builder.mark();

        if (builder.getTokenType() == FLOW_DOC_MARKER) {
            builder.advanceLexer();
        }

        while (builder.getTokenType() == WHITESPACE ||
                builder.getTokenType() == INDENT ||
                builder.getTokenType() == EOL) {
            builder.advanceLexer();
        }

        parseFlowDocDescription(builder);

        skipFlowDocJunk(builder);
        if (builder.getTokenType() == FLOW_DOC_SECTION_HEADER) {
            String sectionName = builder.getTokenText();
            if ("in:".equals(sectionName)) {
                parseFlowDocSection(builder, FlowDocElementTypes.FLOW_DOC_IN_SECTION);
            }
        }

        skipFlowDocJunk(builder);
        if (builder.getTokenType() == FLOW_DOC_SECTION_HEADER) {
            String sectionName = builder.getTokenText();
            if ("out:".equals(sectionName)) {
                parseFlowDocSection(builder, FlowDocElementTypes.FLOW_DOC_OUT_SECTION);
            }
        }

        skipFlowDocJunk(builder);

        if (builder.getTokenType() == FLOW_DOC_MARKER) {
            builder.advanceLexer();
        } else {
            builder.error("Expected closing ## marker");
        }

        docMarker.done(FlowDocElementTypes.FLOW_DOCUMENTATION);
    }

    private static void skipFlowDocJunk(PsiBuilder builder) {
        while (builder.getTokenType() == WHITESPACE ||
                builder.getTokenType() == INDENT ||
                builder.getTokenType() == EOL ||
                builder.getTokenType() == FLOW_DOC_CONTENT ||
                builder.getTokenType() == FLOW_DOC_TEXT) {

            if (builder.getTokenType() == FLOW_DOC_SECTION_HEADER ||
                    builder.getTokenType() == FLOW_DOC_MARKER) {
                break;
            }
            builder.advanceLexer();
        }
    }

    private static void parseFlowDocDescription(PsiBuilder builder) {
        if (builder.getTokenType() != FLOW_DOC_CONTENT) {
            return;
        }

        PsiBuilder.Marker descMarker = builder.mark();

        skipFlowDocJunk(builder);

        descMarker.done(FlowDocElementTypes.FLOW_DOC_DESCRIPTION);
    }

    private static void parseFlowDocSection(PsiBuilder builder, IElementType sectionType) {
        PsiBuilder.Marker sectionMarker = builder.mark();

        if (builder.getTokenType() == FLOW_DOC_SECTION_HEADER) {
            builder.advanceLexer();
        }

        while (builder.getTokenType() == WHITESPACE ||
                builder.getTokenType() == INDENT ||
                builder.getTokenType() == EOL) {
            builder.advanceLexer();
        }

        while (builder.getTokenType() == FLOW_DOC_PARAM_NAME) {
            parseFlowDocParameter(builder);

            while (builder.getTokenType() == WHITESPACE ||
                    builder.getTokenType() == INDENT ||
                    builder.getTokenType() == EOL) {
                builder.advanceLexer();
            }
        }

        sectionMarker.done(sectionType);
    }

    private static void parseFlowDocParameter(PsiBuilder builder) {
        PsiBuilder.Marker paramMarker = builder.mark();

        if (builder.getTokenType() == FLOW_DOC_PARAM_NAME) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == FLOW_DOC_TYPE ||
                builder.getTokenType() == FLOW_DOC_ARRAY_TYPE) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == FLOW_DOC_MANDATORY) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == FLOW_DOC_TEXT) {
            builder.advanceLexer();
        }

        paramMarker.done(FlowDocElementTypes.FLOW_DOC_PARAMETER);
    }
}
