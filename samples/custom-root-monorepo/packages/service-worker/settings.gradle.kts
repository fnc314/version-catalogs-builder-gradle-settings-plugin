plugins {
  id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "2.0.3"
}

versionCatalogsBuilder {
  catalogs {
    register("libs")
  }
}

rootProject.name = "service-worker"
