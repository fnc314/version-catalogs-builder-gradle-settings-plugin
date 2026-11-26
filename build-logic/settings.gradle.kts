enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("./../gradle/libs.versions.toml"))
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "version-catalogs-builder-gradle-settings-plugin-build-logic"

include(":project-extensions")
include(":project-plugins")
include(":generator")
