# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IntelliJ plugin (written in Scala 3) that injects environment variables into JVM Run Configurations by executing an external command before launch. The command is configured per-project via a `.env-inject` file in the project root.

## Build Commands

```sh
./gradlew buildPlugin          # Build the plugin (zip in build/distributions/)
./gradlew check                # Run tests
./gradlew verifyPlugin         # Run IntelliJ Plugin Verifier
./gradlew clean buildPlugin    # Clean + full build
```

Requires Java 21. Uses Gradle 9.3.1 with configuration cache and build cache enabled.

## Architecture

Three Scala 3 source files in `src/main/scala/com/github/jwbargsten/envinject/`:

- **EnvInjectRunConfigurationExtension** — The core plugin class. Extends IntelliJ's `RunConfigurationExtension` to hook into JVM launch. Checks for `.env-inject` file existence (`isEnabledFor`), then runs the external command and merges `KEY=VALUE` pairs into `JavaParameters` (`updateJavaParameters`).
- **EnvInjectConfig** — Reads the `.env-inject` config file from the project root. Returns the first non-empty, non-comment line as the command to execute.
- **ExternalCommandRunner** — Executes the shell command as a subprocess with a 10-second timeout. Parses stdout lines in `KEY=VALUE` format into a `Map[String, String]`.

The plugin is registered via `src/main/resources/META-INF/plugin.xml` as a `runConfigurationExtension`. It depends on `com.intellij.modules.java`.

## Key Configuration

- `gradle.properties` — Plugin version, target IntelliJ platform version (`platformVersion`), minimum build (`pluginSinceBuild`)
- `gradle/libs.versions.toml` — Dependency versions (Scala 3, JUnit, IntelliJ Platform plugin)
- Plugin description is extracted from the `<!-- Plugin description -->` section in README.md during build
