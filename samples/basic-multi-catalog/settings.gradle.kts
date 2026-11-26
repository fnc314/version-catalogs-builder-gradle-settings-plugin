plugins {
  id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "2.0.3"
}

versionCatalogsBuilder {
  catalogs {
    // Standard catalog mapping to gradle/libs.versions.toml
    register("libs")
    // Custom catalog mapping to an explicit file path
    register("tools", "gradle/tools.versions.toml")
  }
}

rootProject.name = "sample-basic-multi-catalog"

include(":app")
include(":core")
