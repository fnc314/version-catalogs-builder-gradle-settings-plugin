# Module version-catalog-builder-gradle-settings-plugin

## About

When applied to a `settings.gradle[.kts]` file, this `org.gradle.api.Plugin` orchestrates workspace root discovery, version catalog loading, and tool build inclusion.

## Usage

See how to declare a dependency upon the plugin and consume/apply it.

### Usage - Plugin Application

The plugin is applied in `settings.gradle[.kts]` files
```gradle
plugins {
    id("dev.fnc314.gradle.plugins.settings.version-catalog-builder-gradle-settings-plugin") version("$latest_version")
}
```

## Source

The source code is available [on `GitHub`](https://github.com/fnc314/version-catalog-builder-gradle-settings-plugin).

# Package dev.fnc314.gradle.plugins.settings.versioncatalogbuildergradlesettingsplugin

Settings convention plugin for multi-project Gradle builds and version catalog builder orchestration.
