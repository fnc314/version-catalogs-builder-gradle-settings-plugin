# Module version-catalogs-builder-gradle-settings-plugin

## About

When applied to a `settings.gradle[.kts]` file, this `org.gradle.api.Plugin` orchestrates workspace root discovery, repository defaults, version catalog loading, and tool build inclusion for multi-project or multi-composite Gradle builds.

### Adopted in Production
This plugin is actively utilized in [`ai-job-tracker`](https://github.com/fnc314/ai-job-tracker) to orchestrate multi-project version catalogs and composite build tools across its Kotlin and Gradle sub-builds.

## Setup

### Plugin Application

The plugin is applied in `settings.gradle[.kts]`:

```kotlin
plugins {
    id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "2.1.0"
}
```

Or via a Version Catalog (`libs.versions.toml`):

```toml
[plugins]
version-catalogs-builder = { id = "dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin", version = "2.1.0" }
```

```kotlin
// settings.gradle.kts
plugins {
    alias(libs.plugins.version.catalogs.builder)
}
```

## Usage

Configure the plugin in `settings.gradle[.kts]` via the `versionCatalogsBuilder` block:

```kotlin
// settings.gradle.kts
versionCatalogsBuilder {
    // Optional override. By default, auto-resolved via:
    // 1. VERSION_CATALOGS_BUILDER_ROOT environment variable
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

- `VERSION_CATALOGS_BUILDER_ROOT`: Dedicated path override for standalone extraction, isolated tool builds, and custom project root targets.
- `WORKSPACE_ROOT`: Standard continuous integration and monorepo workspace path override.

## Source

The source code is available on [GitHub](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin).

# Package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

Settings convention plugin for multi-project Gradle builds and version catalog builder orchestration.
