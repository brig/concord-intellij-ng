# Project Scopes

The Concord IntelliJ plugin supports multiple isolated Concord projects within a single IntelliJ project window. This is achieved through the concept of **Scopes**.

## What is a Scope?

A **Scope** is defined by a "root" Concord file (typically `concord.yml` or `concord.yaml`) and includes all files that belong to that specific project configuration.

### Root Files
A root file is identified by specific naming conventions:
- `concord.yml`
- `concord.yaml`
- `.concord.yml`
- `.concord.yaml`

A scope encompasses the directory containing the root file and any additional resources defined within it.

## Scope Resolution & Isolation

When you have multiple Concord projects (e.g., in a mono-repo structure), the plugin ensures that references (like `call` steps) resolve to the correct files within the same scope.

### Example Structure

```text
my-monorepo/
├── project-a/
│   ├── concord.yaml        <-- Root for Scope A
│   └── concord/
│       └── utils.yaml      <-- Part of Scope A
├── project-b/
│   ├── concord.yaml        <-- Root for Scope B
│   └── concord/
│       └── utils.yaml      <-- Part of Scope B
└── other-project/
    └── concord.yaml        <-- Root for Scope C
```

In this example:
- A `call: utilsFlow` inside `project-a/concord.yaml` will correctly resolve to `project-a/concord/utils.yaml`.
- It will **not** see or suggest flows from `project-b`.
- Global searches (Find Usages, Go to Symbol) are also scoped to respect these boundaries.

## Configuration

By default, a root file automatically includes files matching the pattern:
`concord/{**/,}{*.,}concord.{yml,yaml}` relative to the root file's directory.

You can customize which files are included in a scope by defining `resources` in your root file:

```yaml
resources:
  concord:
    - "glob:flows/*.concord.yaml"
    - "glob:scripts/**/*.yml"

flows:
  myFlow:
    - call: subFlow  # Will look for subFlow in the files matched above
```

### Supported Patterns
- **Glob**: `glob:path/to/**/*.yaml` (Standard glob syntax)
- **Regex**: `regex:path/to/.*\.yaml` (Java regex syntax)
- **Exact path**: `path/to/file.yaml`

## Nested Scopes

If a file matches the patterns of a parent root file, it is considered part of that parent's scope and is **not** treated as a separate root, even if it is named `concord.yaml`.

However, if a `concord.yaml` is found that is *not* covered by any other root's patterns, it becomes a new, independent root.
