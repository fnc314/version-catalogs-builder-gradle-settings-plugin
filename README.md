# Version Catalogs Builder Gradle Settings Plugin

Convention settings plugin for multi-project Gradle builds and version catalog builder orchestration.

[![CI](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-3.0.0-blue.svg)](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin/releases)

## Overview

When designing multi-project Gradle architectures, monorepos, or isolated composite builds, sub-builds often need access to centralized version catalogs (`libs`, `tools`, `local`). 

The `version-catalogs-builder-gradle-settings-plugin` focuses strictly on **one single responsibility**: discovering your workspace root and registering version catalogs into Gradle's `dependencyResolutionManagement`.

It deliberately does **NOT** configure repository handlers, enable feature previews, or register included builds.

### Key Capabilities

1. **Automatic Root Discovery**: Discovers the workspace root by looking for `gradle/wrapper/gradle-wrapper.properties` or `.git`, with environment variable fallbacks (`VERSION_CATALOGS_BUILDER_ROOT`, `WORKSPACE_ROOT`).
2. **Declarative Version Catalog Registration**: Register standard catalogs with a single line (`catalog("libs")`) or custom relative/explicit files (`catalog("tools", "gradle/tools.versions.toml")`).
3. **Natural Gradle File Resolution**: Supports file notation (`String`, `File`, `Path`) passed through `Settings.file` resolution.
4. **DSL Marker Safety**: Annotated with `@VersionCatalogsBuilderDsl` to prevent improper receiver scope leakage in Kotlin DSL settings scripts.

## Setup

In your `settings.gradle.kts`:

```kotlin
plugins {
    id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "3.0.0"
}
```

Or declare it in your `settings.gradle.kts` using a version catalog:

```toml
# gradle/libs.versions.toml
[plugins]
version-catalogs-builder = { id = "dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin", version = "3.0.0" }
```

## Usage

```kotlin
// settings.gradle.kts
versionCatalogsBuilder {
    // Optional workspace root override. By default, auto-resolved via:
    // 1. VERSION_CATALOGS_BUILDER_ROOT environment variable
    // 2. WORKSPACE_ROOT environment variable
    // 3. Upwards filesystem traversal for gradle/wrapper/gradle-wrapper.properties or .git
    // projectRoot.set(rootDir)

    // Implicit convention: resolves to $projectRoot/gradle/libs.versions.toml
    catalog("libs")

    // Explicit relative path (resolved relative to projectRoot)
    catalog("tools", "gradle/tools.versions.toml")

    // Natural file resolution notation:
    catalog("custom", file("config/custom.versions.toml"))
}
```

## Environment Variables

- `VERSION_CATALOGS_BUILDER_ROOT`: Dedicated workspace root override for isolated tool builds, tests, or subprojects.
- `WORKSPACE_ROOT`: Standard continuous integration or monorepo workspace path override.

## License

MIT
