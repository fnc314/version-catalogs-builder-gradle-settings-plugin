plugins {
  id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "2.0.3"
}

versionCatalogsBuilder {
  catalogs {
    register("libs")
  }
  toolBuilds {
    // Include custom convention plugin build
    include("build-logic")
  }
}

rootProject.name = "sample-composite-tool-builds"

include(":library")
