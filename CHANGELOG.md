<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# env-inject Changelog

## [Unreleased]

## [0.0.4] - 2026-05-18

### Changed

- Build against IntelliJ Platform 2026.1.2 (still supports 2025.2+)
- Bump intelliJPlatform gradle plugin 2.11.0 → 2.16.0, Scala 3.8.1 → 3.8.3, qodana 2025.3.1 → 2026.1.0, Gradle 9.3.1 → 9.5.1

## [0.0.3] - 2026-03-14

### Added

- Persist env-inject toggle state across IDE restarts

## [0.0.2] - 2026-03-07

### Fixed

- Fix UnsupportedOperationException when injecting env vars into run configurations with unmodifiable env maps (e.g. ZIO tests)

## [0.0.1-alpha.2] - 2026-02-19

### Added

- Toggle button to switch env-inject on/off
- Added `script/env-inject` for shell interoperability
- Tests

## [0.0.1-alpha.1] - 2026-02-19

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

[Unreleased]: https://github.com/jwbargsten/env-inject/compare/0.0.4...HEAD
[0.0.4]: https://github.com/jwbargsten/env-inject/compare/0.0.3...0.0.4
[0.0.3]: https://github.com/jwbargsten/env-inject/compare/0.0.2...0.0.3
[0.0.2]: https://github.com/jwbargsten/env-inject/compare/0.0.1-alpha.2...0.0.2
[0.0.1-alpha.2]: https://github.com/jwbargsten/env-inject/compare/0.0.1-alpha.1...0.0.1-alpha.2
[0.0.1-alpha.1]: https://github.com/jwbargsten/env-inject/commits/0.0.1-alpha.1
