// SPDX-License-Identifier: Apache-2.0
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

        skipWhitespaceAndPrefix(builder);

        parseFlowDocDescription(builder);

        // Parse sections in any order (in: and out: can appear in any order)
        skipFlowDocJunk(builder);
        while (builder.getTokenType() == FLOW_DOC_SECTION_HEADER) {
            String sectionName = builder.getTokenText();
            if ("in:".equals(sectionName)) {
                parseFlowDocSection(builder, FlowDocElementTypes.FLOW_DOC_IN_SECTION);
            } else if ("out:".equals(sectionName)) {
                parseFlowDocSection(builder, FlowDocElementTypes.FLOW_DOC_OUT_SECTION);
            } else {
                // Unknown section header - skip it
                builder.advanceLexer();
            }
            skipFlowDocJunk(builder);
        }

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
                builder.getTokenType() == FLOW_DOC_TEXT ||
                builder.getTokenType() == FLOW_DOC_COMMENT_PREFIX) {

            if (builder.getTokenType() == FLOW_DOC_SECTION_HEADER ||
                    builder.getTokenType() == FLOW_DOC_MARKER) {
                break;
            }
            builder.advanceLexer();
        }
    }

    private static void parseFlowDocDescription(PsiBuilder builder) {
        // Description starts with FLOW_DOC_CONTENT (after FLOW_DOC_COMMENT_PREFIX and WHITESPACE are skipped)
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

        skipToParamOrEnd(builder);

        while (builder.getTokenType() == FLOW_DOC_PARAM_NAME) {
            parseFlowDocParameter(builder);
            skipToParamOrEnd(builder);
        }

        sectionMarker.done(sectionType);
    }

    private static void skipToParamOrEnd(PsiBuilder builder) {
        while (builder.getTokenType() == WHITESPACE ||
                builder.getTokenType() == INDENT ||
                builder.getTokenType() == EOL ||
                builder.getTokenType() == FLOW_DOC_COMMENT_PREFIX) {

            // If this is a FLOW_DOC_COMMENT_PREFIX, check if it's part of the next section header.
            // Token sequence for "# out:" is: FLOW_DOC_COMMENT_PREFIX, WHITE_SPACE, FLOW_DOC_SECTION_HEADER
            // Don't consume the # if it belongs to the next section.
            if (builder.getTokenType() == FLOW_DOC_COMMENT_PREFIX) {
                var next = builder.lookAhead(1);
                if (next == WHITESPACE) {
                    next = builder.lookAhead(2);
                }
                if (next == FLOW_DOC_SECTION_HEADER || next == FLOW_DOC_MARKER) {
                    break; // This # belongs to the next section or closing marker
                }
            }

            builder.advanceLexer();
        }
    }

    private static void skipWhitespaceAndPrefix(PsiBuilder builder) {
        while (builder.getTokenType() == WHITESPACE ||
                builder.getTokenType() == INDENT ||
                builder.getTokenType() == EOL ||
                builder.getTokenType() == FLOW_DOC_COMMENT_PREFIX) {
            builder.advanceLexer();
        }
    }

    private static void parseFlowDocParameter(PsiBuilder builder) {
        PsiBuilder.Marker paramMarker = builder.mark();

        if (builder.getTokenType() == FLOW_DOC_PARAM_NAME) {
            builder.advanceLexer();
        }

        skipParamWhitespaceAndPunctuation(builder);

        if (builder.getTokenType() == FLOW_DOC_TYPE ||
                builder.getTokenType() == FLOW_DOC_ARRAY_TYPE) {
            builder.advanceLexer();
        }

        skipParamWhitespaceAndPunctuation(builder);

        if (builder.getTokenType() == FLOW_DOC_MANDATORY ||
                builder.getTokenType() == FLOW_DOC_OPTIONAL ||
                builder.getTokenType() == FLOW_DOC_UNKNOWN_KEYWORD) {
            builder.advanceLexer();
        }

        skipParamWhitespaceAndPunctuation(builder);

        if (builder.getTokenType() == FLOW_DOC_TEXT) {
            builder.advanceLexer();
        }

        paramMarker.done(FlowDocElementTypes.FLOW_DOC_PARAMETER);
    }

    private static void skipParamWhitespaceAndPunctuation(PsiBuilder builder) {
        while (builder.getTokenType() == WHITESPACE ||
                builder.getTokenType() == FLOW_DOC_COLON ||
                builder.getTokenType() == FLOW_DOC_COMMA) {
            builder.advanceLexer();
        }
    }
}
