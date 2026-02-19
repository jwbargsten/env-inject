# env-inject

![Build](https://github.com/jwbargsten/env-inject/workflows/Build/badge.svg)

<!-- Plugin description -->
IntelliJ plugin that injects environment variables into JVM Run Configurations
(Java, Kotlin, Scala) by calling an external command before launch. The command
outputs `KEY=VALUE` lines to stdout, which get merged into the process
environment. Configuration is per-project via a `.env-inject` file in the
project root.

This is useful for AI coding agents so they cannot upload your secrets by accident.
<!-- Plugin description end -->

## Usage

Create a `.env-inject` file in your project root. The first non-empty,
non-comment line is the command to execute:

```
# Command that outputs KEY=VALUE lines to stdout
vault kv get -format=export secret/myapp
```

The command runs before every JVM launch (including gradle targets). Its stdout is
parsed for `KEY=VALUE` lines, which are added to the run configuration's environment.

## Example

If you are on MacOS, you might want to start your script or command using the shell:

```sh
/bin/zsh -c "./provide-secrets.py"
```

That makes sure that you have the user environment set up correctly.

My `./provide-secrets.py` looks like this:

```python
#!/usr/bin/env python
import base64


def get_pass(key: str): ...


def b64(v: str):
    return base64.b64encode(v.encode("utf-8")).decode("utf-8")


chain = b64(get_pass("intellij/publish/chain"))
private_key = b64(get_pass("intellij/publish/private-key"))

key_pw = b64(get_pass("intellij/publish/private-key-pw"))
publish_token = get_pass("intellij/publish/token")

print(f"""\
CERTIFICATE_CHAIN={chain}
PRIVATE_KEY={private_key}
PRIVATE_KEY_PASSWORD={key_pw}
PUBLISH_TOKEN={publish_token}""")
```

## Screenshots

![./docs/Screenshot01.png]

![./docs/Screenshot02.png]

![./docs/Screenshot03.png]


## Building

```sh
./gradlew clean buildPlugin
```

The plugin zip is written to `build/distributions/env-inject-<version>.zip`.

## Installation

<kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>gear icon</kbd> > <kbd>Install Plugin from Disk...</kbd> > select the zip from `build/distributions/`

Restart the IDE after installation.
