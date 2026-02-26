# Code Review Style Guide

## Project Context

This is an IntelliJ plugin for Concord YAML files. It uses Java 21, Kotlin 2.3.x, and the IntelliJ Platform SDK.

## Code Style Rules

- All `if`, `for`, `while`, and other control flow statements must use braces `{}`, even for single-line bodies. No braceless `if (x) return null;`.
- Do NOT write trivial class/method comments like "This class does X" or "Returns the value of Y" — if it's obvious from the name, skip the comment.
- DO write comments for non-obvious logic: workarounds, subtle invariants, "why" behind a decision, edge cases, platform quirks.
- Prefer `// why` over `// what` — explain reasoning, not mechanics.

## Architecture Notes

- Plugin extension points are registered in `src/main/resources/META-INF/plugin.xml`
- The plugin uses a forked/embedded YAML implementation in `brig.concord.yaml.*` rather than depending on IntelliJ's YAML plugin
- `ConcordMetaTypeProvider` is the central service that resolves expected schema types for PSI elements — used by inspections, completion, and documentation
- Task schemas use JSON Schema (draft-07) in `src/main/resources/taskSchema/`
- `src/main/gen/` contains generated lexer source — do not review generated code

## Review Focus

- Correctness of IntelliJ PSI manipulation
- Proper null handling (PSI methods frequently return null)
- Thread safety (read/write actions, EDT vs background threads)
- Proper use of IntelliJ caching (CachedValueProvider, modification trackers)
