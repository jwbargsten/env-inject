# env-inject

![Build](https://github.com/jwbargsten/env-inject/workflows/Build/badge.svg)

<!-- Plugin description -->
IntelliJ plugin that injects environment variables into JVM Run Configurations
(Java, Kotlin, Scala) by calling an external command before launch. The command
outputs `KEY=VALUE` lines to stdout, which get merged into the process
environment. Configuration is per-project via a `.env-inject` file in the
project root.
<!-- Plugin description end -->

## Usage

Create a `.env-inject` file in your project root. The first non-empty,
non-comment line is the command to execute:

```
# Command that outputs KEY=VALUE lines to stdout
vault kv get -format=export secret/myapp
```

The command runs before every JVM launch. Its stdout is parsed for `KEY=VALUE`
lines, which are added to the run configuration's environment.

## Building

```sh
./gradlew clean buildPlugin
```

The plugin zip is written to `build/distributions/env-inject-<version>.zip`.

## Installation

<kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>gear icon</kbd> > <kbd>Install Plugin from Disk...</kbd> > select the zip from `build/distributions/`

Restart the IDE after installation.
