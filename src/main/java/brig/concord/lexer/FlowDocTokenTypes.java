// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import brig.concord.yaml.YAMLElementType;
import com.intellij.psi.tree.IElementType;

public interface FlowDocTokenTypes {

    IElementType FLOW_DOC_MARKER = new YAMLElementType("FLOW_DOC_MARKER");
    IElementType FLOW_DOC_COMMENT_PREFIX = new YAMLElementType("FLOW_DOC_COMMENT_PREFIX");
    IElementType FLOW_DOC_SECTION_HEADER = new YAMLElementType("FLOW_DOC_SECTION_HEADER");
    IElementType FLOW_DOC_PARAM_NAME = new YAMLElementType("FLOW_DOC_PARAM_NAME");
    IElementType FLOW_DOC_TYPE = new YAMLElementType("FLOW_DOC_TYPE");
    IElementType FLOW_DOC_ARRAY_TYPE = new YAMLElementType("FLOW_DOC_ARRAY_TYPE");
    IElementType FLOW_DOC_MANDATORY = new YAMLElementType("FLOW_DOC_MANDATORY");
    IElementType FLOW_DOC_OPTIONAL = new YAMLElementType("FLOW_DOC_OPTIONAL");
    /** Unknown keyword at mandatory/optional position (typos like "mandatry") */
    IElementType FLOW_DOC_UNKNOWN_KEYWORD = new YAMLElementType("FLOW_DOC_UNKNOWN_KEYWORD");
    IElementType FLOW_DOC_TEXT = new YAMLElementType("FLOW_DOC_TEXT");
    IElementType FLOW_DOC_CONTENT = new YAMLElementType("FLOW_DOC_CONTENT");
    IElementType FLOW_DOC_COLON = new YAMLElementType("FLOW_DOC_COLON");
    IElementType FLOW_DOC_COMMA = new YAMLElementType("FLOW_DOC_COMMA");
}
