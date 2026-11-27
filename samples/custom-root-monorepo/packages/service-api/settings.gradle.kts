plugins {
  id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "3.1.1"
}

versionCatalogsBuilder {
  catalog("libs")
}

rootProject.name = "service-api"
