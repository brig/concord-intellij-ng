# Concord DSL — Flow Documentation Blocks

This document describes how to document flows in `concord.yml` files using
**flow documentation blocks**.

Flow documentation blocks allow you to describe what a flow does, its input
parameters, and its outputs. This information is used by the Concord IntelliJ
plugin for navigation, inspections, and future editor features.

---

## What is a flow documentation block?

A flow documentation block is a structured comment block placed **immediately
before a flow definition** under `flows:`.

It is written entirely using YAML comments and does **not** affect runtime
execution.

---

## 1. Basic format

A documentation block is wrapped between two lines containing only `##`.

```yaml
flows:
  ##
  # Process S3 files and return the total count
  # in:
  #   s3Bucket: string, mandatory, S3 bucket name
  #   prefix: string, optional, File prefix filter
  # out:
  #   processed: int, mandatory, Files processed count
  ##
  processS3:
    - task: s3
```

### Rules

- The block **must start** with a line containing only `##`
- Each content line **must start** with `# ` (hash + space)
- The block **must end** with another `##`
- The block **must be placed directly above** the flow name
- Only one documentation block is allowed per flow

---

## 2. Flow description

All comment lines **before** the first `in:` or `out:` section are treated as
the flow description.

- Multiple lines are allowed
- Empty comment lines are ignored
- The description may span several paragraphs

Example:

```yaml
##
# Process S3 files and upload results.
# This flow scans a bucket and processes matching files.
#
# in:
#   bucket: string, mandatory
##
process:
  - log: "test"
```

---

## 3. Input and output parameters

Parameters are declared inside `in:` and `out:` sections.

**Important:** Parameter lines must have **extra indentation** (2+ spaces after
`#`). Lines with only 1 space after `#` are treated as user text and ignored.

### Parameter format

```
#   name: type, mandatory|optional, description
```

Only the parameter name and type are required.

- `mandatory` / `optional` is optional
- If omitted, the parameter is treated as **optional**
- The description is optional but strongly recommended

Example:

```yaml
##
# in:
#   s3Bucket: string, mandatory, S3 bucket name
#   prefix: string, optional, File prefix filter
# out:
#   processed: int, mandatory, Files processed count
##
processS3:
  - task: s3
```

---

## 4. Supported types

### Simple types

- `string`
- `int`
- `number`
- `boolean`
- `object`
- `any`

### Array types

- `string[]`
- `int[]`
- `number[]`
- `boolean[]`
- `object[]`
- `any[]`

---

## 5. Nested object parameters

Nested object fields are expressed using **dot notation** in the parameter name.

This allows documenting structured inputs without additional syntax.

```yaml
##
# in:
#   config: object, mandatory, Connection configuration
#   config.host: string, mandatory, Server host
#   config.port: int, optional, Server port
##
connect:
  - task: connect
```

---

## 6. Custom tags and user text

Lines inside `in:` or `out:` sections that do **not** have extra indentation
(i.e., only one space after `#`) are treated as user text and ignored.

This allows you to add custom tags, notes, or additional metadata without
affecting parameter parsing.

```yaml
##
# Process S3 files
# in:
#   s3Bucket: string, mandatory, S3 bucket name
# tags: internal, deprecated
# see: https://example.com/docs
##
processS3:
  - task: s3
```

In this example:
- `s3Bucket` is parsed as a parameter (has 3 spaces after `#`)
- `tags:` and `see:` are ignored (only 1 space after `#`)

---

## 7. Notes and limitations

- Documentation blocks are parsed **only** when placed directly above a flow
  under `flows:`
- Documentation blocks are comments and **do not affect execution**
- Parameters must be indented with **2 or more spaces** after `#`
- Lines with only 1 space after `#` inside sections are treated as user text
- Unknown or custom types are treated as free text
- Malformed lines are ignored gracefully
