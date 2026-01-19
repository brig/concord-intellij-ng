# Concord DSL — Syntax Highlighting Model

This document describes the semantic categories used for syntax highlighting
in the Concord DSL IntelliJ plugin.

The goal of the model is:
- semantic correctness (names reflect real DSL meaning),
- consistency across sections (flows / imports / triggers),
- extensibility without introducing new colors unnecessarily,
- support for structured documentation embedded in comments.

---

## 1. High-level principles

- Reserved DSL elements are highlighted consistently across the whole file.
- User-defined keys are visually distinct from DSL-defined keys.
- Color usage is intentionally limited to avoid visual noise.
- Different semantic roles may reuse the same color with different font styles.
- Documentation blocks are treated as structured content, not plain comments.
- Documentation highlighting must never compete visually with executable DSL code.

---

## 2. Highlighting categories

### 2.1 Top-level DSL sections

**Constant**
```
CONCORD_DSL_SECTION
```

**Examples**
```yaml
flows:
configuration:
imports:
triggers:
```

**Meaning**  
Structural anchors of the document. Define major DSL sections.

---

### 2.2 Flow identifiers

**Constant**
```
CONCORD_FLOW_IDENTIFIER
```

**Examples**
```yaml
flows:
  main:
  deploy:
```

**Meaning**  
User-defined flow names. Identifiers of executable flow blocks.

---

### 2.3 Step keywords

**Constant**
```
CONCORD_STEP_KEYWORD
```

**Examples**
```yaml
- task:
- call:
- log:
- script:
- set:
- return:
```

**Meaning**  
Action keywords. Represent executable steps in a flow.

---

### 2.4 DSL kinds (imports / triggers)

**Constant**
```
CONCORD_DSL_KIND
```

**Examples**
```yaml
imports:
  - git:
  - mvn:

triggers:
  github:
  cron:
```

**Meaning**  
Declares the *kind* of import or trigger.
Defines which DSL schema applies to the nested configuration.

---

### 2.5 Predefined DSL keys

**Constant**
```
CONCORD_DSL_KEY
```

**Examples**
```yaml
runtime:
debug:
url:
path:
version:
dest:
exclude:
secret:
in:
out:
retry:
loop:
error:
entryPoint:
ignoreEmptyPush:
```

**Meaning**  
Keys that are predefined by the Concord DSL schema.
They have a known semantic meaning and validation rules.

---

### 2.6 DSL labels (`name`)

**Constant**
```
CONCORD_DSL_LABEL
```

**Examples**
```yaml
name: "Download data from S3"
```

**Meaning**  
Human-readable labels.
Semantically DSL keys, but visually separated to improve readability.

---

### 2.7 Step targets (values)

**Constant**
```
CONCORD_TARGET_IDENTIFIER
```

**Examples**
```yaml
- task: downloadData
- call: otherFlow
```

**Meaning**  
Target identifiers referenced by steps.
These are values, not keys.

---

### 2.8 Expressions

**Constant**
```
CONCORD_EXPRESSION
```

**Examples**
```yaml
${amount > 0}
${url}
```

**Meaning**  
Embedded expressions. Highlighted softly to avoid overpowering structure.

---

### 2.9 Literals

| Constant        | Examples    |
|-----------------|-------------|
| CONCORD_STRING  | "text"      |
| CONCORD_NUMBER  | 1, 3.14     |
| CONCORD_BOOLEAN | true, false |
| CONCORD_NULL    | null, ~     |

---

### 2.10 Comments

**Constant**
```
CONCORD_COMMENT
```

**Examples**
```yaml
# this is a comment
```

**Meaning**  
Unstructured comments with no semantic meaning.

---

## 2.11 Flow documentation (structured comments)

Flow documentation is a structured, semantically rich block embedded in YAML
comments. It is not executable DSL, but it describes the input/output contract of a flow.

Documentation blocks are delimited by a marker and interpreted as a separate
documentation layer.

Documentation blocks are parsed and highlighted separately from regular DSL
elements, but share the same lexical space.

---

### 2.11.1 Documentation marker

**Constant**
```
CONCORD_FLOW_DOC_MARKER
```

**Examples**
```yaml
##
```

**Meaning**  
Marks the beginning and end of a flow documentation block.  
Acts as a visual anchor for the documentation region.

---

### 2.11.2 Documentation comment prefix

**Constant**
```
CONCORD_FLOW_DOC_COMMENT_PREFIX
```

**Examples**
```yaml
#
```

**Meaning**  
YAML comment prefix used inside a documentation block.
Rendered with minimal visual weight to reduce YAML noise.

---

### 2.11.3 Documentation sections

**Constant**
```
CONCORD_FLOW_DOC_SECTION
```

**Examples**
```yaml
# in:
# out:
```

**Meaning**  
Structural sections of flow documentation.  
Define input and output parameter groups.

---

### 2.11.4 Documentation parameter names

**Constant**
```
CONCORD_FLOW_DOC_PARAM_NAME
```

**Examples**
```yaml
# file:
# interpolateArgs:
```

**Meaning**  
Names of input or output parameters.  
This is the primary visual anchor within documentation lines.

---

### 2.11.5 Documentation parameter types

**Constant**
```
CONCORD_FLOW_DOC_TYPE
```

**Examples**
```yaml
# string
# object
# boolean
```

**Meaning**  
Declared parameter data types.  
Used for documentation, validation, and future tooling support.

---

### 2.11.6 Documentation modifiers

**Constants**
```
CONCORD_FLOW_DOC_MANDATORY
CONCORD_FLOW_DOC_OPTIONAL
```

**Examples**
```yaml
# mandatory
# optional
```

**Meaning**  
Parameter cardinality modifiers.  
Indicate whether a parameter is required or optional.

---

### 2.11.7 Documentation text / descriptions

**Constant**
```
CONCORD_FLOW_DOC_TEXT
```

**Examples**
```yaml
# k8s manifest file to delete
```

**Meaning**  
Free-form human-readable descriptions.  
Visually subdued to avoid competing with parameter names.

---

### 2.11.8 Documentation punctuation

**Constant**
```
CONCORD_FLOW_DOC_PUNCTUATION
```

**Examples**
```yaml
# :
# ,
```

**Meaning**  
Separators inside documentation lines.  
Purely syntactic, visually minimized.

---

### 2.12 User-defined keys

**Constant**
```
CONCORD_USER_KEY
```

**Meaning**  
Keys not recognized as part of the Concord DSL schema.

---

### 2.13 Structural / auxiliary tokens

These tokens are purely syntactic and do not carry DSL semantics.

| Constant                | Meaning                          |
|-------------------------|----------------------------------|
| `CONCORD_COLON`         | Colon separator (`:`)            |
| `CONCORD_BRACKETS`      | Brackets and braces (`[]`, `{}`) |
| `CONCORD_BAD_CHARACTER` | Invalid or unexpected characters |


## 3. Summary

| Category              | Constant                        |
|-----------------------|---------------------------------|
| Top-level sections    | CONCORD_DSL_SECTION             |
| Flow names            | CONCORD_FLOW_IDENTIFIER         |
| Step keywords         | CONCORD_STEP_KEYWORD            |
| Import / Trigger kind | CONCORD_DSL_KIND                |
| Predefined DSL keys   | CONCORD_DSL_KEY                 |
| Labels                | CONCORD_DSL_LABEL               |
| Step targets          | CONCORD_TARGET_IDENTIFIER       |
| Expressions           | CONCORD_EXPRESSION              |
| Comments              | CONCORD_COMMENT                 |
| User keys             | CONCORD_USER_KEY                |
| Doc marker            | CONCORD_FLOW_DOC_MARKER         |
| Doc comment prefix    | CONCORD_FLOW_DOC_COMMENT_PREFIX |
| Doc section           | CONCORD_FLOW_DOC_SECTION        |
| Doc param name        | CONCORD_FLOW_DOC_PARAM_NAME     |
| Doc type              | CONCORD_FLOW_DOC_TYPE           |
| Doc mandatory         | CONCORD_FLOW_DOC_MANDATORY      |
| Doc optional          | CONCORD_FLOW_DOC_OPTIONAL       |
| Doc text              | CONCORD_FLOW_DOC_TEXT           |
| Doc punctuation       | CONCORD_FLOW_DOC_PUNCTUATION    |

---

## 4. Notes

- Documentation highlighting is intentionally subtle.
- Documentation must never visually dominate executable DSL code.
- No additional colors should be introduced unless a new semantic role appears.
- New DSL features should map to existing categories whenever possible.
- Flow documentation tokens are allowed to reuse comment-adjacent colors only.
- Visual consistency is preferred over maximal differentiation.
- Documentation tokens must not introduce new parsing ambiguities in YAML.
