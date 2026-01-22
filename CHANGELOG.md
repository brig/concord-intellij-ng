# concord-intellij-plugin Changelog

## Unreleased

### Added

- added quick fix feature for unknown flow documentation keywords, enabling users to automatically correct typos in mandatory and optional keywords
([#50](https://github.com/brig/concord-intellij-ng/pull/50))  
- added quick fix to replace invalid parameter types with valid suggestions in flow documentation
([#49](https://github.com/brig/concord-intellij-ng/pull/49))  
- added quick fix for missing closing ## marker in flow documentation
([#45](https://github.com/brig/concord-intellij-ng/pull/45))  
- added rename refactoring support for Concord flow definitions
([#44](https://github.com/brig/concord-intellij-ng/pull/44)) 
- added a quick fix feature that allows users to view all duplicate flow definitions in a usage view panel
([#43](https://github.com/brig/concord-intellij-ng/pull/43)) 
- added inspection to detect duplicate flow names
([#42](https://github.com/brig/concord-intellij-ng/pull/42))
- added project scopes to support multiple isolated Concord projects within a single IntelliJ project window
([#40](https://github.com/brig/concord-intellij-ng/pull/40))
- test coverage for cron expression folding functionality and code cleanup associated code cleanup
([#39](https://github.com/brig/concord-intellij-ng/pull/39))
- implemented autocompletion support for flow documentation syntax in Concord YAML files
([#37](https://github.com/brig/concord-intellij-ng/pull/37))
- implemented inspection for flow documentation blocks in Concord YAML files
([#36](https://github.com/brig/concord-intellij-ng/pull/36)) 
- implemented syntax highlighting for flow documentation blocks in Concord YAML files
([#35](https://github.com/brig/concord-intellij-ng/pull/35)) 
- allow custom text/tags in flow documentation
([#34](https://github.com/brig/concord-intellij-ng/pull/34)) 
- support for flow documentation blocks in Concord YAML files
([#31](https://github.com/brig/concord-intellij-ng/pull/31))

### Changed

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
- removed unused YAML syntax highlighting files and refactored `DuplicatedKeysInspection` inspection and tests
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
