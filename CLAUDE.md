# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IntelliJ plugin (Scala 3) that injects environment variables into JVM Run Configurations by executing external commands before launch. Commands are configured per-project via a `.env-inject` file in the project root.

## Build Commands

```sh
./gradlew buildPlugin          # Build the plugin (zip in build/distributions/)
./gradlew check                # Run tests
./gradlew verifyPlugin         # Run IntelliJ Plugin Verifier
./gradlew clean buildPlugin    # Clean + full build
```

Requires Java 21. Uses Gradle 9.3.1 with configuration cache and build cache enabled.

## Architecture

Four Scala 3 source files in `src/main/scala/org/bargsten/envinject/`:

- **EnvInjectRunConfigurationExtension** — The core plugin class. Extends IntelliJ's `RunConfigurationExtension` to hook into JVM launch. Checks for `.env-inject` file existence (`isEnabledFor`), then runs all configured commands, merges their `KEY=VALUE` output, and injects the result into `JavaParameters`. Shows balloon notifications on failure.
- **EnvInjectConfig** — Reads the `.env-inject` config file from the project root. Returns all non-empty, non-comment lines as commands to execute (supports multiple commands).
- **ExternalCommandRunner** — Executes shell commands as subprocesses with a 7-second timeout. Uses `shlex.split` to tokenize the command string, then parses stdout lines in `KEY=VALUE` format into a `Map[String, String]`.
- **shlex** — POSIX shell-style command tokenizer (port of Python's `shlex.split`). Handles single/double quotes, backslash escaping, comments, and whitespace splitting. Also provides `shlex.quote`.

The plugin is registered via `src/main/resources/META-INF/plugin.xml` as a `runConfigurationExtension` with a `notificationGroup` for balloon warnings. It depends on `com.intellij.modules.java`.

## Key Configuration

- `gradle.properties` — Plugin version, target IntelliJ platform version (`platformVersion`), minimum build (`pluginSinceBuild`)
- `gradle/libs.versions.toml` — Dependency versions (Scala 3, JUnit, IntelliJ Platform plugin)
- Plugin description is extracted from the `<!-- Plugin description -->` section in README.md during build
