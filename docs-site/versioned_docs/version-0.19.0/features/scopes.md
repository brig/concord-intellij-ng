---
title: Multiple Scopes
description: Support for monorepos and multiple Concord projects
---

import Keystroke from '@site/src/components/Keystroke';
import Link from '@docusaurus/Link';

# Multiple Scopes

Modern development often involves "monorepos" where multiple independent applications or microservices live in the same repository.

The Concord IntelliJ Plugin fully supports this workflow through **Scopes**. It automatically detects and isolates independent Concord projects within a single IntelliJ workspace.

## How it Works

The plugin scans your project for "root" configuration files (e.g., `concord.yaml`). Each root file defines a **Scope**.

All symbol resolution, code completion, and refactoring operations are strictly confined to the current scope. This prevents "pollution" where a flow named `deploy` in **Project A** accidentally shows up in **Project B**.

### Root Files

A scope is automatically created for any directory containing one of these files:
*   `concord.yml`
*   `concord.yaml`
*   `.concord.yml`
*   `.concord.yaml`

## Monorepo Example

Consider a repository with two separate projects:

```text
my-monorepo/
├── billing-service/
│   ├── concord.yaml        <-- Root for Scope A
│   └── concord/
│       └── common.yaml     <-- Part of Scope A
│
├── inventory-service/
│   ├── concord.yaml        <-- Root for Scope B
│   └── concord/
│       └── common.yaml     <-- Part of Scope B
```

If you edit `billing-service/concord.yaml`:
1.  **Navigation:** <Keystroke mac={[{icon: "⌘", label: "Cmd"}]} win={[{label: "Ctrl"}]} /> + **Click** on a flow call will only look inside `billing-service/`.
2.  **Completion:** You will only see flows defined in **Scope A**.
3.  **Validation:** Errors in `inventory-service` will not appear in the context of `billing-service`.

## Customizing Resources

By default, a scope includes the root file and any files inside the `concord/` directory (standard Concord convention).

You can include additional files in a scope using the standard `resources` directive in your `concord.yaml`:

```concord
resources:
  concord:
    # Include all YAML files in the 'flows' directory
    - "glob:flows/**/*.concord.yaml"

flows:
  main:
    # The plugin will now find 'setupFlow' defined in scripts/setup.yml
    - call: setupFlow
```

### Supported Patterns

*   **Glob:** `glob:path/**/*.yaml`
*   **Regex:** `regex:path/.*\.yaml`
*   **Exact:** `path/to/file.yaml`

For more details on pattern syntax, see the <Link to="https://concord.walmartlabs.com/docs/processes-v2/resources.html" className="external-link">official Concord documentation</Link>.

:::note Nested Scopes
If a file is covered by the `resources` pattern of a parent `concord.yaml`, it becomes part of that parent scope. It does not start a new scope, even if it is named `concord.yaml`.
:::

## Files Outside Scope

If you open a Concord file that does not belong to any defined scope (it is not a root file, is not matched by any `resources` pattern, or [Ignored](#ignored-files)), the IDE will display a notification:

> **File outside Concord scope**
> This file is not in any Concord scope. Flow navigation and other features may be limited.

### Why this happens
This usually means the file is "orphaned." Because the plugin doesn't know which project this file belongs to, it cannot safely resolve flow calls or provide accurate completion.

### How to fix
1.  **If this is a new project:** Create a `concord.yaml` file in the project root.
2.  **If this is part of an existing project:** Update the `resources` section of your main `concord.yaml` to include this file path (e.g., add `"glob:scripts/**/*.yml"`).

## Ignored Files

The plugin respects your version control settings. Files that are ignored by Git (e.g., listed in `.gitignore`) are automatically **excluded** from all Concord scopes.

*   They will not trigger "File outside Concord scope" warnings.
*   They will not participate in flow resolution or completion.
*   They will not appear in search results or refactoring operations.

This is particularly useful for temporary files, build artifacts, or local overrides that shouldn't be part of the project's static analysis.

## Global Search

When using features like **Search Everywhere** (Double Shift) or **Go to Symbol**, you might see multiple flows with the same name across different projects.

To help you distinguish between them, the plugin displays the **Scope Name** (the name of the directory containing the root file) next to each result:

*   `deployApp` `[billing-service]`
*   `deployApp` `[inventory-service]`

## Uniqueness
Scopes act as isolated namespaces.

:::tip Allowed
You can define a flow named <code>utils</code> in Scope A and another <code>utils</code> in Scope B.
:::

:::warning Not allowed
Two flows named <code>utils</code> in the same scope (even across files) will trigger a validation error.
:::
