plugins {
  id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "3.1.2"
}

versionCatalogsBuilder {
  catalog("libs")
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

rootProject.name = "sample-multi-project-shared-catalog"

include(":app", ":core")
