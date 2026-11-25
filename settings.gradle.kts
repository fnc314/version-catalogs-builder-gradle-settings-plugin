/*
 * Settings convention plugin root build
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")

  plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "version-catalogs-builder-gradle-settings-plugin-root"

include(":plugin")
project(":plugin").name = "version-catalogs-builder-gradle-settings-plugin"
