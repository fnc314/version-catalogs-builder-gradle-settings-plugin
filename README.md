# Version Catalog Builder Gradle Settings Plugin

Convention settings plugin for multi-project Gradle builds and version catalog builder orchestration.

[![CI](https://github.com/fnc314/version-catalog-builder-gradle-settings-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/fnc314/version-catalog-builder-gradle-settings-plugin/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/fnc314/version-catalog-builder-gradle-settings-plugin/releases)

## Overview

When building multi-composite or multi-repo Gradle architectures, sub-builds often need access to centralized version catalogs (`libs`, `local`, `tools`) and shared convention plugins. This plugin orchestrates:

1. Automatic resolution of the monorepo / project root.
2. Configuration of plugin and dependency resolution repositories (`google()`, `mavenCentral()`, `gradlePluginPortal()`).
3. Loading of requested Version Catalogs into `dependencyResolutionManagement`.
4. Inclusion of shared tool builds into `pluginManagement`.

## Setup

```kotlin
// settings.gradle.kts
plugins {
    id("dev.fnc314.gradle.plugins.settings.version-catalog-builder-gradle-settings-plugin") version "1.0.0"
}
```

Or with a Version Catalog:

```toml
[plugins]
version-catalog-builder = { id = "dev.fnc314.gradle.plugins.settings.version-catalog-builder-gradle-settings-plugin", version = "1.0.0" }
```

## Usage

```kotlin
// settings.gradle.kts
versionCatalogBuilder {
    // Optional override. By default, auto-resolved via:
    // 1. GRADLE_DOCS_ROOT environment variable
    // 2. WORKSPACE_ROOT environment variable
    // 3. Upwards filesystem traversal for gradle/wrapper/gradle-wrapper.properties or .git
    // projectRoot.set(file("/path/to/root"))

    catalogs {
        // Abbreviated syntax: register("name") implicitly maps to "gradle/name.versions.toml"
        register("libs")
        register("tools")

        // Long-form syntax: register("name", "relativePath") explicitly maps to a custom relative file path
        register("local", "gradle/local.versions.toml")
        register("customCatalog", "config/catalogs/custom.versions.toml")
    }

    toolBuilds {
        // Includes composite builds relative to projectRoot
        include("gradle/tools/plugins-projects")
        include("gradle/tools/plugins-docs")
    }
}
```

## Environment Variables

- `GRADLE_DOCS_ROOT`: Primary path override for standalone extraction and documentation pipelines.
- `WORKSPACE_ROOT`: Standard continuous integration workspace path override.

## License

MIT
