# IntelliJ Plugin for Concord

![Build](https://github.com/brig/concord-intellij-ng/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/20365.svg)](https://plugins.jetbrains.com/plugin/20365)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/20365.svg)](https://plugins.jetbrains.com/plugin/20365)

<!-- Plugin description -->
IntelliJ plugin for [Concord](https://github.com/walmartlabs/concord).

Transforms IntelliJ IDEA into a powerful IDE for Concord flows with:
*   **Schema validation & Inspections**
*   **Smart Code Completion**
*   **Navigation & Call Hierarchy**
*   **Safe Refactoring**
*   **Structured Documentation Support**
<!-- Plugin description end -->

---

## 📚 Documentation

Detailed documentation, feature guides, and examples are available at:

### 👉 **[https://brig.github.io/concord-intellij-ng/](https://brig.github.io/concord-intellij-ng/)**

---

## Key Features

*   **Smart Editing:** Context-aware completion for keys, steps, and parameters.
*   **Validation:** Real-time checking for unknown keys, type mismatches, and missing requirements.
*   **Navigation:** Jump to flow definitions (`Ctrl+B`) and find usages (`Alt+F7`) instantly across your entire project.
*   **Refactoring:** Safely rename flows (`Shift+F6`) with automatic updates of all references.
*   **Documentation:** Support for structured flow documentation blocks with parameter metadata.
*   **Structure View:** Navigate large files easily with a tree view of flows and triggers.
*   **Scopes:** Full support for monorepos with multiple isolated Concord projects.

---

## Installation

### Using the IDE built-in plugin system

<kbd>Settings/Preferences</kbd> → <kbd>Plugins</kbd> → <kbd>Marketplace</kbd> → <kbd>Search for "Concord"</kbd> → <kbd>Install Plugin</kbd>

### Manual installation

Download the [latest release](https://github.com/brig/concord-intellij-ng/releases/latest) and install it using:

<kbd>Settings/Preferences</kbd> → <kbd>Plugins</kbd> → <kbd>⚙️</kbd> → <kbd>Install plugin from disk...</kbd>

---

## Development

### Running Performance Tests

To run automated performance tests and collect a CPU profile (JFR):

```bash
./gradlew :perf-tester:runIde
```

The test will automatically:
1. Open a set of Concord files (including a large stress-test file).
2. Trigger code analysis and highlighting.
3. Collect JFR data and exit.

**Result:** The JFR report will be generated at `build/reports/perf-<timestamp>.jfr` (e.g. `perf-20260226-121530.jfr`). You can open this file in IntelliJ IDEA or Java Mission Control to analyze CPU usage and allocations.
