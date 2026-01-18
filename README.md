# IntelliJ Plugin for Concord

![Build](https://github.com/brig/concord-intellij-ng/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/20365.svg)](https://plugins.jetbrains.com/plugin/20365)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/20365.svg)](https://plugins.jetbrains.com/plugin/20365)

<!-- Plugin description -->
IntelliJ plugin for [Concord](https://github.com/walmartlabs/concord)

Smart Concord YAML support with schema-aware completion, inspections and validation,
semantic highlighting, navigation between flows and forms, structure view,
and flow documentation blocks.
<!-- Plugin description end -->

---

## Features

- Concord-aware syntax highlighting for sections, steps, keys, and expressions
- Schema-driven completion for Concord YAML fields (flows, steps, imports, triggers, params)
- Inspections and validation for unknown keys, missing required keys, duplicates, and invalid values
- Navigation between flow elements, including `call` targets and forms
- Structure view for flows, forms, and triggers
- Structured flow documentation blocks with parameter metadata

---

## Documentation

This plugin supports **flow documentation blocks** — structured comment blocks
placed directly above flow definitions in `concord.yml` files.

They allow you to describe:
- what a flow does
- its input parameters
- its output parameters

Example:

~~~yaml
flows:
  ##
  # Process S3 files and return the total count
  # in:
  #   s3Bucket: string, mandatory, S3 bucket name
  # out:
  #   processed: int, mandatory, Files processed count
  ##
  processS3:
    - task: s3
~~~

📘 **Full documentation:**
- [Flow documentation blocks](docs/flow-documentation.md)

---

## Installation

### Using the IDE built-in plugin system

<kbd>Settings/Preferences</kbd> →
<kbd>Plugins</kbd> →
<kbd>Marketplace</kbd> →
<kbd>Search for "Concord"</kbd> →
<kbd>Install Plugin</kbd>

### Manual installation

Download the [latest release](https://github.com/brig/test-plugin/releases/latest)
and install it using:

<kbd>Settings/Preferences</kbd> →
<kbd>Plugins</kbd> →
<kbd>⚙️</kbd> →
<kbd>Install plugin from disk...</kbd>
