// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.lexer;

import brig.concord.yaml.YAMLElementType;
import com.intellij.psi.tree.IElementType;

public interface FlowDocElementTypes {

    IElementType FLOW_DOCUMENTATION = new YAMLElementType("FLOW_DOCUMENTATION");
    IElementType FLOW_DOC_DESCRIPTION = new YAMLElementType("FLOW_DOC_DESCRIPTION");
    IElementType FLOW_DOC_IN_SECTION = new YAMLElementType("FLOW_DOC_IN_SECTION");
    IElementType FLOW_DOC_OUT_SECTION = new YAMLElementType("FLOW_DOC_OUT_SECTION");
    IElementType FLOW_DOC_PARAMETER = new YAMLElementType("FLOW_DOC_PARAMETER");
}
