# Caching Strategy in Concord IntelliJ Plugin

This document describes the caching mechanisms used within the plugin to ensure high performance and responsiveness, 
particularly for code analysis, completion, and resolving references in potentially large YAML files.

## Overview

The plugin leverages the IntelliJ Platform's `CachedValuesManager` to store derived data attached to PSI elements or the project. 
This allows expensive computations (like resolving flow references or building meta-type models) to be performed only when necessary.

Data invalidation is handled via `CachedValueProvider.Result` dependencies. 
When a dependency (like a file, a PSI element, or the global modification tracker) changes, the cache is automatically invalidated, 
and the value is recomputed upon the next access.

## Cache Inventory

### 1. Meta-Type & Documentation Caches

These caches are crucial for the "Smart" features of the plugin: validation, completion, and documentation lookups.

#### `FlowCallParamsProvider`
Handles caching of Flow Documentation (`## ...`) and its mapping to YAML types.

| Cache Key                  | Scope                            | Value                      | Dependencies                                | Description                                                                                                                                                                                                                      |
|----------------------------|----------------------------------|----------------------------|---------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CALL_SITE_DOC_CACHE`      | `YAMLKeyValue` (call site)       | `FlowDocumentation` (PSI)  | `PsiModificationTracker.MODIFICATION_COUNT` | Stores the resolved documentation for a specific `call: flowName` line. Invalidates on **any** PSI change to ensure the reference is always up-to-date while typing. Avoids repeated `resolve()` calls during inspection passes. |
| `FLOW_DOC_CACHE`           | `YAMLKeyValue` (flow definition) | `FlowDocumentation` (PSI)  | `PsiFile` (of definition)                   | Stores the documentation block associated with a specific flow definition key. Invalidates only when the file containing the flow definition changes.                                                                            |
| `FLOW_DOC_META_TYPE_CACHE` | `FlowDocumentation` (PSI)        | `FlowDocMetaType` (Object) | `FlowDocumentation`                         | Caches the computed MetaType object derived from the documentation comments. Prevents re-parsing comments into types on every access.                                                                                            |

#### `YamlMetaTypeProvider`
The backbone of the type system (used by `ConcordMetaTypeProvider`).

| Cache Key | Scope | Value | Dependencies | Description |
|-----------|-------|-------|--------------|-------------|
| *Dynamic* (`getClass().getName() + ":KEY"`) | `YAMLValue` | `MetaTypeProxy` | `PsiFile` + `ModificationTracker` | Caches the inferred type for any YAML element. In `ConcordMetaTypeProvider`, the tracker is `PsiManager.getInstance(project).getModificationTracker()`, meaning types are re-evaluated on any PSI change to support dynamic schema updates (like QuickFixes). |

### 2. Reference Resolution Caches

Used to speed up `Go to Declaration` and `Find Usages`.

#### `FlowDefinitionReference`

| Cache Key         | Scope                          | Value              | Dependencies                                            | Description                                                                                                                                                                 |
|-------------------|--------------------------------|--------------------|---------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `FLOW_DEFS_CACHE` | `YAMLScalar` (flow name usage) | `List<PsiElement>` | `PsiModificationTracker.MODIFICATION_COUNT` + `element` | Caches the results of searching for flow definitions. Uses the global modification counter because adding a flow definition in *another* file should invalidate this cache. |

#### `CallInParamDefinitionReference`

| Cache Key            | Scope                        | Value                           | Dependencies                                            | Description                                                                                                                                           |
|----------------------|------------------------------|---------------------------------|---------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `IN_PARAM_DEF_CACHE` | `YAMLKeyValue` (param usage) | `PsiElement` (param definition) | `PsiModificationTracker.MODIFICATION_COUNT` + `element` | Caches the resolution of an input parameter key back to its definition in the `in:` section of the flow documentation. Invalidates on any PSI change. |

### 3. Scope & File Indexing Caches

#### `ConcordScopeService`
Manages the visibility of Concord files across the project.

| Cache Key                                 | Scope   | Value                           | Dependencies                  | Description                                                                                                                                                                                                                                                            |
|-------------------------------------------|---------|---------------------------------|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| *Project Service* (`findRoots`)           | Project | `List<ConcordRoot>`             | `VFS_STRUCTURE_MODIFICATIONS` | Caches the list of detected Concord root files.                                                                                                                                                                                                                        |
| *Project Service* (`getAllMatchingFiles`) | Project | `Map<String, Set<VirtualFile>>` | `VFS_STRUCTURE_MODIFICATIONS` | Caches the mapping of roots to all files included in their scope. **Note:** Currently depends only on VFS structure. Changes to `resources` patterns within `concord.yaml` content might not trigger invalidation immediately (requires VFS change or project reload). |
| *Project Service* (`findAllConcordFiles`) | Project | `Collection<VirtualFile>`       | `VFS_STRUCTURE_MODIFICATIONS` | Caches the list of all files matching Concord naming patterns in the project.                                                                                                                                                                                          |

### 4. Low-Level PSI Caches

#### `YAMLLocalResolveUtil`

| Cache Key  | Scope     | Value                     | Dependencies | Description                                                                                                          |
|------------|-----------|---------------------------|--------------|----------------------------------------------------------------------------------------------------------------------|
| *Internal* | `PsiFile` | `Map<String, YAMLAnchor>` | `PsiFile`    | Caches a map of all YAML anchors in a file to speed up alias resolution. Invalidates when the specific file changes. |

#### `YAMLBlockScalarImpl` (Kotlin)

| Cache Key  | Scope                 | Value             | Dependencies                | Description                                                                                       |
|------------|-----------------------|-------------------|-----------------------------|---------------------------------------------------------------------------------------------------|
| *Internal* | `YAMLBlockScalarImpl` | `List<TextRange>` | `this` (The element itself) | Caches the text ranges of a block scalar content. Uses `ReadActionCachedValue` for thread safety. |
