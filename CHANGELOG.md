# Changelog

## Unreleased

### Added

- adds comprehensive documentation infrastructure for Concord YAML elements
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
