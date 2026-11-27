plugins {
  id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "3.1.1"
}

versionCatalogsBuilder {
  // Standard catalog mapping to gradle/libs.versions.toml
  catalog("libs")
  // Custom catalog mapping to an explicit file path
  catalog("tools", "gradle/tools.versions.toml")
}

rootProject.name = "sample-basic-multi-catalog"

include(":app")
include(":core")
