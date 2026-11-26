# Version Catalogs Builder Gradle Settings Plugin

Convention settings plugin for multi-project Gradle builds and version catalog builder orchestration.

[![CI](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-3.0.0-blue.svg)](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin/releases)

## Overview

When designing multi-project Gradle architectures, monorepos, or isolated composite builds, sub-builds often need access to centralized version catalogs (`libs`, `tools`, `local`). 

The `version-catalogs-builder-gradle-settings-plugin` focuses strictly on **one single responsibility**: discovering your workspace root and registering version catalogs into Gradle's `dependencyResolutionManagement`.

It deliberately does **NOT** configure repository handlers, enable feature previews, or register included builds.

### Key Capabilities

1. **Automatic Root Discovery**: Discovers the workspace root by looking for `gradle/wrapper/gradle-wrapper.properties` or `.git`, with `gradle.properties` (`dev.fnc314.version-catalogs-builder.root`) and environment variable fallbacks (`VERSION_CATALOGS_BUILDER_ROOT`, `WORKSPACE_ROOT`).
2. **Declarative Version Catalog Registration**:
   - Convention by name: `catalog("libs")` (defaults to `gradle/libs.versions.toml`).
   - Derived name from file notation: `catalog("gradle/network-tools.versions.toml")` (automatically registers accessor `networkTools`).
   - Explicit name and notation: `catalog("tools", "gradle/tools.versions.toml")`.
3. **Natural Gradle File Resolution**: Supports file notation (`String`, `File`, `Path`) passed through `Settings.file` resolution relative to `projectRoot`.
4. **Configuration-Cache & DSL Safety**: Extension backed by configuration-cache-safe `MapProperty` and annotated with `@VersionCatalogsBuilderDsl` to prevent improper receiver scope leakage in Kotlin DSL settings scripts.
5. **Gradle Problems API Diagnostics**: Emits rich, multi-line problem warnings when a catalog declaration is replaced, comparing prior and subsequent declarations via `replacedBy`.

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
    // 1. dev.fnc314.version-catalogs-builder.root in gradle.properties
    // 2. VERSION_CATALOGS_BUILDER_ROOT environment variable
    // 3. WORKSPACE_ROOT environment variable
    // 4. Upwards filesystem traversal for gradle/wrapper/gradle-wrapper.properties or .git
    // projectRoot.set(rootDir)

    // Implicit convention: resolves to $projectRoot/gradle/libs.versions.toml
    catalog("libs")

    // Inferred name: derives catalog accessor "networkTools" from filename
    catalog("gradle/network-tools.versions.toml")

    // Explicit relative path (resolved relative to projectRoot)
    catalog("tools", "gradle/tools.versions.toml")

    // Natural file resolution notation:
    catalog("custom", file("config/custom.versions.toml"))
}
```

## Configuration & Overrides

### Gradle Properties (`gradle.properties`)

- `dev.fnc314.version-catalogs-builder.root`: Explicit root path override.
- `dev.fnc314.version-catalogs-builder.default-catalog-name`: Overrides default catalog name used for conflict detection with `gradle/libs.versions.toml` (defaults to `dependencyResolutionManagement.defaultLibrariesExtensionName` or `"libs"`).

### Environment Variables

- `VERSION_CATALOGS_BUILDER_ROOT`: Dedicated workspace root override for isolated tool builds, tests, or subprojects.
- `VERSION_CATALOGS_BUILDER_DEFAULT_CATALOG_NAME`: Environment variable override for the default catalog name.
- `WORKSPACE_ROOT`: Standard continuous integration or monorepo workspace path override.

## License

MIT
