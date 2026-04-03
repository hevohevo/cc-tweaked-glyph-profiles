# Building This Fork On macOS

This is a small setup memo for building and running this fork on macOS with Homebrew.

It is intentionally minimal and focuses on the Forge development workflow.

## Requirements

- macOS
- [Homebrew](https://brew.sh/)
- Git
- JDK 17
- Node.js 20

This fork builds against JDK 17. You do not need to install multiple JDK versions just to build and run it.

## Install Dependencies

```bash
brew update
brew install git node@20 openjdk@17
```

On Apple Silicon Homebrew is usually installed under `/opt/homebrew`.

If needed, add the binaries to your shell `PATH`:

```bash
export PATH="/opt/homebrew/opt/node@20/bin:$PATH"
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
```

To make this persistent in `zsh`, add the same lines to `~/.zshrc`.

## Verify Java

```bash
java -version
/usr/libexec/java_home -V
```

If `java -version` does not report JDK 17, point `JAVA_HOME` at it explicitly:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

## Get The Source

```bash
git clone https://github.com/hevohevo/cc-tweaked-glyph-profiles.git
cd cc-tweaked-glyph-profiles
git branch
```

If you already have a local checkout of your fork, use that instead.

If you want to work on a separate branch from your current fork state:

```bash
git switch -c jp-font-fork
```

## Build

```bash
./gradlew build
```

If the build succeeds, the basic toolchain is working.

## Run The Forge Client

```bash
./gradlew :core:processResources :core:shadowJar :forge:runClient
```

This rebuilds the core resources and then starts a Forge development client for the current checkout.

## VS Code

Useful extensions:

- Extension Pack for Java
- Kotlin

## Notes

- Keep secrets such as GitHub personal access tokens out of setup notes and shell history when possible.
- For quick local development, the commands above are usually enough.
- If you are working on the glyph profile resources, you may also find these files useful:
  - [`README.txt`](../projects/core/src/main/resources/assets/computercraft/textures/gui/README.txt)
  - [`README_ja.txt`](../projects/core/src/main/resources/assets/computercraft/textures/gui/README_ja.txt)
