# Module version-catalogs-builder-gradle-settings-plugin

## About

When applied to a `settings.gradle[.kts]` file, this `org.gradle.api.Plugin` orchestrates workspace root discovery and registers version catalogs into Gradle's `dependencyResolutionManagement` for multi-project or multi-composite Gradle builds.

The plugin focuses strictly on **one single responsibility**: building and registering version catalogs. It does not configure repositories, feature preview flags, or tool builds.

### Adopted in Production
This plugin is actively utilized in [`ai-job-tracker`](https://github.com/fnc314/ai-job-tracker) to orchestrate multi-project version catalogs across its Kotlin and Gradle sub-builds.

## Setup

### Plugin Application

The plugin is applied in `settings.gradle[.kts]`:

```kotlin
plugins {
    id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "3.1.2"
}
```

Or via a Version Catalog (`libs.versions.toml`):

```toml
[plugins]
version-catalogs-builder = { id = "dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin", version = "3.1.2" }
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
    // 1. dev.fnc314.version-catalogs-builder.root in gradle.properties
    // 2. VERSION_CATALOGS_BUILDER_ROOT environment variable
    // 3. WORKSPACE_ROOT environment variable
    // 4. Upwards filesystem traversal for gradle/wrapper/gradle-wrapper.properties or .git
    // projectRoot.set(file("/path/to/root"))

    // Convention syntax: maps to "gradle/libs.versions.toml"
    catalog("libs")

    // Inferred name syntax: derives accessor "networkTools" from the filename
    catalog("gradle/network-tools.versions.toml")

    // Explicit path syntax: maps "tools" to a custom relative file path
    catalog("tools", "gradle/tools.versions.toml")

    // File notation syntax:
    catalog("custom", file("config/catalogs/custom.versions.toml"))
}
```

## Configuration & Environment Variables

- `dev.fnc314.version-catalogs-builder.root`: Gradle property for workspace root path override.
- `dev.fnc314.version-catalogs-builder.default-catalog-name`: Gradle property for default catalog name override.
- `VERSION_CATALOGS_BUILDER_ROOT`: Dedicated path override for standalone extraction, isolated tool builds, and custom project root targets.
- `VERSION_CATALOGS_BUILDER_DEFAULT_CATALOG_NAME`: Environment variable override for the default catalog name.
- `WORKSPACE_ROOT`: Standard continuous integration and monorepo workspace path override.

## Source

The source code is available on [GitHub](https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin).

# Package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

Settings convention plugin for multi-project Gradle builds and version catalog builder orchestration.
