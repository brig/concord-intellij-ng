# Changelog

## Unreleased

### Added

### Changed

## 0.20.1 - 2026-02-21

### Added

- updateMetaOnAllEvents to events configuration
  ([#146](https://github.com/brig/concord-intellij-ng/pull/146))
- batchSize and batchFlushInterval to events configuration
  ([#145](https://github.com/brig/concord-intellij-ng/pull/145))
- support for interpreting dotted keys in set: steps (e.g., a.b: 1) as nested object variables for schema inference and variable discovery
  ([#141](https://github.com/brig/concord-intellij-ng/pull/141))

### Changed

- fixes issues with the EL (Expression Language) parser
  ([#144](https://github.com/brig/concord-intellij-ng/pull/144))
- handle quoted return/exit step values
  ([#143](https://github.com/brig/concord-intellij-ng/pull/143))

## 0.20.0 - 2026-02-21

### Added

- autocomplete and navigation support for nested properties in EL (Expression Language) expressions within Concord YAML files
([#136](https://github.com/brig/concord-intellij-ng/pull/136))   
- schema type inference for variables declared in `out` section of task step
([#135](https://github.com/brig/concord-intellij-ng/pull/135))   
- schema type inference for variables declared in `out` section of call step
([#133](https://github.com/brig/concord-intellij-ng/pull/133))   
- extends variable schema inference to `configuration.arguments`
([#132](https://github.com/brig/concord-intellij-ng/pull/132)) 
- schema type inference for variables declared in `set` steps within Concord flow definitions
([#131](https://github.com/brig/concord-intellij-ng/pull/131)) 
- ability to select a specific JDK for running the Concord CLI
([#128](https://github.com/brig/concord-intellij-ng/pull/128)) 
- EL variable navigation support so `${var}` identifiers can Go to Declaration and variable declarations
can Find Usages across common Concord variable sources (args, flow doc params, set, and step out)
([#124](https://github.com/brig/concord-intellij-ng/pull/124)) 
- variable completion support inside EL (Expression Language) expressions in Concord YAML files
([#123](https://github.com/brig/concord-intellij-ng/pull/123)) 
- lexer-level highlighting and parsing for EL (Expression Language) expressions in Concord YAML files
([#121](https://github.com/brig/concord-intellij-ng/pull/121)) 
- variables provider with argument tracking and built-in variables
([#119](https://github.com/brig/concord-intellij-ng/pull/119))
- autocompletion for output variables in the `out` field of `call` steps
([#118](https://github.com/brig/concord-intellij-ng/pull/118))

### Changed

- improved Concord variable completion to better reflect step scoping and Concord-specific variables (loop variables, task results)
([#129](https://github.com/brig/concord-intellij-ng/pull/129))  
- nested object properties support in task schemas
([#125](https://github.com/brig/concord-intellij-ng/pull/125))
- restart daemon on external VFS changes to fix stale highlighting
([#122](https://github.com/brig/concord-intellij-ng/pull/122))
- `TaskRegistry` now checks `VirtualFile` validity before access to prevent potential `InvalidVirtualFileAccessException`
([#116](https://github.com/brig/concord-intellij-ng/pull/116))

## 0.19.0 - 2026-02-14

### Added

- adds an inspection to detect dependencies with server-only version strings like `latest`
or placeholders that cannot be resolved locally by the IDE. Provides a quick fix to add a concrete version to the `cli` profile
([#113](https://github.com/brig/concord-intellij-ng/pull/113))
- add documentation support for completion popup items (lookup elements)
([#111](https://github.com/brig/concord-intellij-ng/pull/111))
- adds comprehensive documentation support for Concord task definitions,
including task-level descriptions, input/output parameter documentation
([#110](https://github.com/brig/concord-intellij-ng/pull/110))
- adds support for propagating flow-doc input parameter metadata (description, type, required) to call-step parameter docs
([#109](https://github.com/brig/concord-intellij-ng/pull/109))
- adds documentation support for flow calls in Concord YAML files
([#107](https://github.com/brig/concord-intellij-ng/pull/107))
- adds documentation for Concord YAML elements
([#106](https://github.com/brig/concord-intellij-ng/pull/106))
- adds JSON-based task schema support to power YAML inspections and completion for task `in`/`out` parameters,
starting with the built-in Concord task schema
([#96](https://github.com/brig/concord-intellij-ng/pull/96))

### Changed

- fix icon for tool window
([#102](https://github.com/brig/concord-intellij-ng/pull/102))
- project: migrate to junit5 
([#101](https://github.com/brig/concord-intellij-ng/pull/101))
- project: intellij plugins version up 
([#95](https://github.com/brig/concord-intellij-ng/pull/95))

## 0.18.1 - 2026-02-07

### Changed

- improve performance by replacing filename-based scanning with indexed lookups and simplifying fingerprint computation
([#98](https://github.com/brig/concord-intellij-ng/pull/98))
- fixes Concord modification tracking in multi-project scenarios by filtering filesystem/document 
events to the current project and hardening PSI lookups
([#97](https://github.com/brig/concord-intellij-ng/pull/97))

## 0.18.0 - 2026-02-07

### Added

- show unresolved dependency errors as inspections and in Build sync tab
([#92](https://github.com/brig/concord-intellij-ng/pull/92))
- added task-name autocompletion backed by Maven dependency scanning,
  reorganizes the Concord tool window tree to display scope files, dependencies, and resources with improved navigation.
([#91](https://github.com/brig/concord-intellij-ng/pull/91))
- added Concord tool window
([#88](https://github.com/brig/concord-intellij-ng/pull/88))
- added a performance testing project for the Concord IntelliJ plugin
([#86](https://github.com/brig/concord-intellij-ng/pull/86))
- added Related Symbol navigation from any Concord file to the project’s root concord.yaml file(s).
([#85](https://github.com/brig/concord-intellij-ng/pull/85))

### Changed

- refactors the Concord tool window implementation to improve performance
([#90](https://github.com/brig/concord-intellij-ng/pull/90))
- ensure flow completion strictly respects search scope
([#87](https://github.com/brig/concord-intellij-ng/pull/87))

## 0.17.0 - 2026-01-30

### Added

- aded Run Configuration support to execute Concord flows locally from the IDE
([#67](https://github.com/brig/concord-intellij-ng/pull/67))   
- added support for respecting `.gitignore` rules in Concord file discovery and scope resolution
([#62](https://github.com/brig/concord-intellij-ng/pull/62))   
- added breadcrumbs functionality
([#65](https://github.com/brig/concord-intellij-ng/pull/65))  
- automatic language injection support for script steps in Concord flows
([#63](https://github.com/brig/concord-intellij-ng/pull/63))  
- added an icon provider for Concord YAML files to display appropriate icons for different sections
and elements in the IDE's structure view and navigation
([#59](https://github.com/brig/concord-intellij-ng/pull/59))  
- added inspection and notification when Concord YAML files are outside their defined scope
([#57](https://github.com/brig/concord-intellij-ng/pull/57))

### Changed

- performance optimizations
([#82](https://github.com/brig/concord-intellij-ng/pull/82)) 
- fix code completion for trigger entry points
([#66](https://github.com/brig/concord-intellij-ng/pull/66))
- fix for completion functionality in multi-scope projects
([#56](https://github.com/brig/concord-intellij-ng/pull/56))

## 0.16.0 - 2026-01-23

### Added

- сall hierarchy feature for Concord flows, allowing developers to view caller and callee relationships between flows
([#52](https://github.com/brig/concord-intellij-ng/pull/52))  
- added a quick fix to add input parameters to flow documentation blocks
([#51](https://github.com/brig/concord-intellij-ng/pull/51))  
- added quick fix feature for unknown flow documentation keywords, enabling users to automatically correct typos in mandatory and optional keywords
([#50](https://github.com/brig/concord-intellij-ng/pull/50))  
- added quick fix to replace invalid parameter types with valid suggestions in flow documentation
([#49](https://github.com/brig/concord-intellij-ng/pull/49))  
- added quick fix for missing closing ## marker in flow documentation

### Changed

- simplified Concord file discovery by relying on IntelliJ’s standard indexing
  instead of custom filtering logic  
([#48](https://github.com/brig/concord-intellij-ng/pull/48))
- support arbitrary indentation in flow documentation
([#45](https://github.com/brig/concord-intellij-ng/pull/45))  
- refactored enter handler functionality, added tests
([#41](https://github.com/brig/concord-intellij-ng/pull/41))  
- updated the CodeStyleSettingsProvider example configuration
([#38](https://github.com/brig/concord-intellij-ng/pull/38))  
- project: improved CI reliability and performance by reducing duplicate runs and hardening build scripts
([#33](https://github.com/brig/concord-intellij-ng/pull/33))  
- improved call in-params reference resolution for quoted keys.
Added caching for flow documentation and in-params reference resolution
([#30](https://github.com/brig/concord-intellij-ng/pull/30))
- improved inspection descriptions for Concord YAML files
([#28](https://github.com/brig/concord-intellij-ng/pull/28))
- removed unused YAML syntax highlighting files and refactored  inspection and tests
([#27](https://github.com/brig/concord-intellij-ng/pull/27))

## 0.15.0 - 2026-01-14

### Added

- implemented syntax highlighting support 
([#25](https://github.com/brig/concord-intellij-ng/pull/25))

### Changed

- updated supported IntelliJ IDEA version (2025.3.1)
([#25](https://github.com/brig/concord-intellij-ng/pull/25))
- fixed navigation error where targets could become invalid

## 0.14.0

- folding for flows
- structure view

## 0.13.0

- folding for cron triggers spec

## 0.10.0

- logYaml step
- extraDependencies in Profile definition
- Intellij version up

## 0.6.2

- Fix publish repository channel

## 0.6.1

- 2024.2 compatibility

## 0.6.0

- intellij idea version up

## 0.5.0

- intellij idea version up

## 0.4.0

- intellij idea version up

## 0.3.0

- flow call input params completion (experimental);

## 0.2.0

- find flow usage;
- gh-triggers: groupBy as string;
- triggers: entryPoint as link to flow definition;
- parser: fix parsing multi-line expressions.

## 0.1.0

- parse quoted strings as plaint text (e.g. `call: myFlow` and `call: "myFlow"` the same now)
- flow definition index per project;
- loop items as links to flow definition.

## 0.0.11

- throw: name attribute;
- idea version up.

## 0.0.10

- indexes for flow search and validate;
- simple search everywhere impl.

## 0.0.9

- analyze only concord files;
- validate steps array;

## 0.0.8

- Idea 2022.3 support;
- Fix `exit` step.

## 0.0.7

### Added

- initial steps documentation;
- default value for autocomplete form field; 
- default value for autocomplete form name;
- default value for autocomplete flow name;
- Support for generic trigger;
- Support for `return` step.

## [0.0.6 - 2022-11-16]

### Changed

- Using proper java version

## [0.0.5 - 2022-11-16]

### Added

- Concord runtime-v2 syntax support

## [0.0.4 - 2022-11-07]

### Added

- Initial version

## [0.0.1 - 2022-11-07]

### Added

- Initial version
