package dev.fnc314.gradle.plugins.settings.versioncatalogbuildergradlesettingsplugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.io.File

/**
 * Settings convention plugin orchestrating workspace root discovery,
 * repository defaults, catalog loading, and tool build inclusion.
 */
public class VersionCatalogBuilderGradleSettingsPlugin : Plugin<Settings> {
  override fun apply(settings: Settings) {
    val extension = settings.extensions.create(
      "versionCatalogBuilder",
      VersionCatalogBuilderGradleSettingsExtension::class.java,
    )

    // Register aliases for DSL flexibility and backwards compatibility
    settings.extensions.add(
      "versionCatalogs",
      extension,
    )
    settings.extensions.add(
      "subsystemSettings",
      extension,
    )

    // Resolve projectRoot
    val resolvedRoot: File = resolveRoot(settings)
    extension.projectRoot.convention(settings.layout.settingsDirectory.dir(resolvedRoot.absolutePath))

    // Enable standard features
    settings.enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

    // Configure repository defaults for pluginManagement & dependencyResolutionManagement
    settings.pluginManagement.repositories { handler ->
      handler.google()
      handler.mavenCentral()
      handler.gradlePluginPortal()
    }

    settings.dependencyResolutionManagement.repositories { handler ->
      handler.google()
      handler.mavenCentral()
    }

    settings.gradle.settingsEvaluated {
      val rootDir = extension.projectRoot.get().asFile

      // Include tool builds
      val toolsToInclude = if (extension.toolBuildsList.isNotEmpty()) {
        extension.toolBuildsList
      } else {
        emptyList()
      }

      for (relPath in toolsToInclude) {
        val buildDir = File(rootDir, relPath)
        if (buildDir.exists()) {
          settings.pluginManagement.includeBuild(buildDir.absolutePath)
        }
      }

      // Configure catalogs
      for ((name, customPath) in extension.catalogsList) {
        val relPath = customPath ?: "gradle/$name.versions.toml"
        val catalogFile = File(rootDir, relPath)
        if (catalogFile.exists() && settings.dependencyResolutionManagement.versionCatalogs.findByName(name) == null) {
          settings.dependencyResolutionManagement.versionCatalogs.create(name) { catalog ->
            catalog.from(catalogFile)
          }
        }
      }
    }
  }

  private fun resolveRoot(settings: Settings): File {
    System.getenv("GRADLE_DOCS_ROOT")?.let { File(it) }?.takeIf { it.exists() }?.let { return it }
    System.getenv("WORKSPACE_ROOT")?.let { File(it) }?.takeIf { it.exists() }?.let { return it }

    // Upwards search for gradle/wrapper/gradle-wrapper.properties or .git
    var current: File? = settings.settingsDir
    while (current != null) {
      if (File(current, "gradle/wrapper/gradle-wrapper.properties").exists() || File(current, ".git").exists()) {
        return current
      }
      current = current.parentFile
    }

    return settings.settingsDir.parentFile ?: settings.settingsDir
  }
}
