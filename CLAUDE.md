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

Eight Scala 3 source files in `src/main/scala/org/bargsten/envinject/`:

- **EnvInjectRunConfigurationExtension** — Extends IntelliJ's `RunConfigurationExtension` to hook into JVM launch. Delegates to `EnvInjectResolver` and injects the resulting env vars into `JavaParameters`. Also handles `ExternalSystemRunConfiguration` (e.g. Gradle tasks) by merging env vars into the run settings.
- **EnvInjectResolver** — Orchestrates env var resolution: reads commands from config, runs each via `ExternalCommandRunner`, merges results, and shows balloon notifications (info on success, warnings on failure).
- **EnvInjectConfig** — Reads the `.env-inject` config file from the project root. Returns non-empty, non-comment lines as commands, tokenized via `shlex.split` into `Either[TokenizeError, CmdSpec]`. Also defines `CmdSpec` (command + working directory).
- **EnvInjectExecutionListener** — `ExecutionListener` that saves/restores original env vars for `ExternalSystemRunConfiguration` runs, ensuring injected vars don't persist across executions.
- **ExternalCommandRunner** — Executes shell commands as subprocesses with a 7-second timeout. Parses stdout lines in `KEY=VALUE` format into a `Map[String, String]`. Returns `Either[CommandExecutionError, Map[String, String]]`.
- **shlex** — POSIX shell-style command tokenizer (port of Python's `shlex.split`). Handles single/double quotes, backslash escaping, comments, and whitespace splitting. Also provides `shlex.quote`.
- **Error** — Defines the `EnvInjectError` sealed trait (currently unused scaffolding).
- **util** — Extension methods: `Either.widen` for error type widening, and `spacesep`/`commasep` on `Seq[String]`/`Set[String]`.

The plugin is registered via `src/main/resources/META-INF/plugin.xml` as a `runConfigurationExtension` and `ExecutionListener`, with notification groups for balloon warnings and info. It depends on `com.intellij.modules.java`.

## Testing

Test framework is **MUnit** (the JUnit dependency is for the MUnit runner integration).

```sh
./gradlew test                                                    # Run all tests
./gradlew test --tests org.bargsten.envinject.ShlexTest           # Run a single test class
```

Test files in `src/test/scala/org/bargsten/envinject/`:
- `EnvInjectResolverTest` — Tests `ExternalCommandRunner.run` with real subprocesses (uses `perl -E`)
- `ShlexTest` — Tests `shlex.quote` for shell escaping correctness

## Code Conventions

- Error handling uses `Either[ErrorType, Result]` pattern throughout (e.g. `Either[CommandExecutionError, Map[String, String]]`)
- Formatted with scalafmt (`.scalafmt.conf`): max 130 columns, Scala 3 dialect
- Uses Scala 3 features: `using` clauses, extension methods, union types (`|`), match expressions, optional braces

## Key Configuration

- `gradle.properties` — Plugin version, target IntelliJ platform version (`platformVersion`), minimum build (`pluginSinceBuild`)
- `gradle/libs.versions.toml` — Dependency versions (Scala 3, JUnit, IntelliJ Platform plugin)
- Plugin description is extracted from the `<!-- Plugin description -->` section in README.md during build

## Style Rules (apply to all output: code, comments, docs, commit messages)

- Be succinct. Say it once, say it short.
- No redundant comments. If the code is clear, don't comment it.
- No filler text, no restating the obvious, no "this function does X" before a function named X.
- When asked to "eliminate repetition" or "remove redundant comments", take it literally.
- No fluff, no fuzzy

## Before Writing Code

- Check if a rough design or architecture decision is needed first. Ask if unclear.
- Design around data structures. Get the data model right before implementing logic around it.
- Develop the critical path first — the hard, fundamental part stripped to essentials.
- Don't introduce abstractions preemptively. Duplication is cheaper than the wrong abstraction. Let patterns emerge.
- Think about module and package structure before creating new packages.
- Don't create fine-grained packages with one class each ("categoritis"). Organise by feature, not by category.
- Don't introduce DTOs if not needed. E.g. if kafka models are generated from an avro spec, you can map directly to domain models without any DTO.

## Writing Code

- One level of abstraction per function. Don't mix high-level orchestration with low-level details.
- Functions should fit on a screen (~80–100 lines max).
- Group code by functional cohesion (things that contribute to the same task), not by class-per-responsibility.
- Keep dependencies minimal. Don't add libraries for trivial tasks.
- No tactical DDD patterns or hexagonal architecture unless explicitly requested.
- If you don't know a library, read its docs or source on GitHub. Don't guess the API.

## Testing

- Write integration and e2e tests early. They catch what AI misses — AI reasons locally, tests verify globally.
- For UI: write Selenium e2e tests first. Use them to verify and self-correct.
- One test per desired external behavior, plus one test per bug.
- Tests target the API of a cohesive unit — not individual classes or internal methods.
- Use tests to find edge cases.
- Don't write tests before the implementation exists (no TDD).

## APIs and Interfaces

- Treat APIs as permanent. Don't change signatures without explicit approval.
- Be strict in what you accept and what you return. Don't silently tolerate malformed input.
- Minimize observable behavior surface — anything observable will be depended on.

## Conventions and Consistency

- Follow existing patterns in the codebase. When in doubt, match what's already there.
- Global project structure matters. Local style within a function or module is flexible.
- If a convention exists (naming, structure, patterns), follow it. Don't introduce alternatives.

## AI Workflow

- Don't over-engineer prompts or plans. Work with what's given plainly.
- After producing code, expect it to be reviewed and challenged. ~50% of output will need major changes.
- Never commit secrets, credentials, or API keys.
- When fixing bugs: reproduce with a test first, then fix.
- If a task is ambiguous, ask one clarifying question rather than guessing.
