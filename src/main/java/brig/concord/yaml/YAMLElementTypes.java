package brig.concord.yaml;

import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.lexer.FlowDocTokenTypes;
import brig.concord.parser.ConcordYAMLParserDefinition;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

public interface YAMLElementTypes {
    YAMLElementType DOCUMENT = new YAMLElementType("Document ---");

    YAMLElementType KEY_VALUE_PAIR = new YAMLElementType("Key value pair");
    //YAMLElementType VALUE = new YAMLElementType("Value");
    YAMLElementType HASH = new YAMLElementType("Hash");
    YAMLElementType ARRAY = new YAMLElementType("Array");
    YAMLElementType SEQUENCE_ITEM = new YAMLElementType("Sequence item");
    YAMLElementType COMPOUND_VALUE = new YAMLElementType("Compound value");
    YAMLElementType MAPPING = new YAMLElementType("Mapping");
    YAMLElementType SEQUENCE = new YAMLElementType("Sequence");
    YAMLElementType SCALAR_LIST_VALUE = new YAMLElementType("Scalar list value");
    YAMLElementType SCALAR_TEXT_VALUE = new YAMLElementType("Scalar text value");
    YAMLElementType SCALAR_PLAIN_VALUE = new YAMLElementType("Scalar plain style");
    YAMLElementType SCALAR_QUOTED_STRING = new YAMLElementType("Scalar quoted string");
    YAMLElementType ANCHOR_NODE = new YAMLElementType("Anchor node");
    YAMLElementType ALIAS_NODE = new YAMLElementType("Alias node");

    TokenSet BLOCK_SCALAR_ITEMS = TokenSet.create(
            YAMLTokenTypes.SCALAR_LIST,
            YAMLTokenTypes.SCALAR_TEXT
    );

    TokenSet TEXT_SCALAR_ITEMS = TokenSet.create(
            YAMLTokenTypes.SCALAR_STRING,
            YAMLTokenTypes.SCALAR_DSTRING,
            YAMLTokenTypes.TEXT
    );

    TokenSet SCALAR_ITEMS = TokenSet.orSet(BLOCK_SCALAR_ITEMS, TEXT_SCALAR_ITEMS);

    TokenSet SCALAR_VALUES = TokenSet.orSet(SCALAR_ITEMS, TokenSet.create(
            SCALAR_LIST_VALUE
    ));

    TokenSet EOL_ELEMENTS = TokenSet.create(
            YAMLTokenTypes.EOL,
            YAMLTokenTypes.SCALAR_EOL
    );

    TokenSet SPACE_ELEMENTS = TokenSet.orSet(EOL_ELEMENTS, TokenSet.create(
            YAMLTokenTypes.WHITESPACE,
            TokenType.WHITE_SPACE,
            YAMLTokenTypes.INDENT
    ));

    TokenSet BLANK_ELEMENTS = TokenSet.orSet(SPACE_ELEMENTS, TokenSet.create(
            YAMLTokenTypes.COMMENT
    ));

    // Flow documentation elements (treated as comments for formatting purposes)
    TokenSet FLOW_DOC_ELEMENTS = TokenSet.create(
            FlowDocElementTypes.FLOW_DOCUMENTATION,
            FlowDocElementTypes.FLOW_DOC_DESCRIPTION,
            FlowDocElementTypes.FLOW_DOC_IN_SECTION,
            FlowDocElementTypes.FLOW_DOC_OUT_SECTION,
            FlowDocElementTypes.FLOW_DOC_PARAMETER,
            FlowDocTokenTypes.FLOW_DOC_MARKER,
            FlowDocTokenTypes.FLOW_DOC_SECTION_HEADER,
            FlowDocTokenTypes.FLOW_DOC_PARAM_NAME,
            FlowDocTokenTypes.FLOW_DOC_TYPE,
            FlowDocTokenTypes.FLOW_DOC_ARRAY_TYPE,
            FlowDocTokenTypes.FLOW_DOC_MANDATORY,
            FlowDocTokenTypes.FLOW_DOC_TEXT,
            FlowDocTokenTypes.FLOW_DOC_CONTENT
    );

    TokenSet CONTAINERS = TokenSet.create(
            SCALAR_LIST_VALUE,
            SCALAR_TEXT_VALUE,
            DOCUMENT,
            SEQUENCE,
            MAPPING,
            SCALAR_QUOTED_STRING,
            SCALAR_PLAIN_VALUE
    );

    TokenSet BRACKETS = TokenSet.create(
            YAMLTokenTypes.LBRACE,
            YAMLTokenTypes.RBRACE,
            YAMLTokenTypes.LBRACKET,
            YAMLTokenTypes.RBRACKET
    );

    TokenSet DOCUMENT_BRACKETS = TokenSet.create(
            YAMLTokenTypes.DOCUMENT_MARKER,
            YAMLTokenTypes.DOCUMENT_END
    );

    TokenSet TOP_LEVEL = TokenSet.create(
             ConcordYAMLParserDefinition.FILE,
            DOCUMENT
    );

    TokenSet INCOMPLETE_BLOCKS = TokenSet.create(
            MAPPING,
            SEQUENCE,
            COMPOUND_VALUE,
            SCALAR_LIST_VALUE,
            SCALAR_TEXT_VALUE
    );

    TokenSet YAML_COMMENT_TOKENS = TokenSet.create(YAMLTokenTypes.COMMENT);

    TokenSet WHITESPACE_TOKENS = TokenSet.create(YAMLTokenTypes.WHITESPACE);
}
