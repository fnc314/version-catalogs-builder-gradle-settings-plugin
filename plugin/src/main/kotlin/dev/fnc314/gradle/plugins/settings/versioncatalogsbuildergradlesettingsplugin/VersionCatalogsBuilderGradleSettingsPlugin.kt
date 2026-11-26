package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Path

/**
 * Settings convention plugin orchestrating workspace root discovery and version catalog registration.
 */
public class VersionCatalogsBuilderGradleSettingsPlugin : Plugin<Settings> {
  /**
   * Applies the version catalogs builder plugin to the given [Settings] target.
   *
   * Resolves the workspace [VersionCatalogsBuilderGradleSettingsExtension.projectRoot],
   * creates the `versionCatalogsBuilder` extension, and schedules version catalog
   * registration on settings evaluation.
   *
   * @param settings The Gradle [Settings] instance being configured.
   */
  override fun apply(settings: Settings) {
    val extension = settings.extensions.create(
      "versionCatalogsBuilder",
      VersionCatalogsBuilderGradleSettingsExtension::class.java,
    )

    // Resolve projectRoot
    val resolvedRoot: File = resolveRoot(settings)
    extension.projectRoot.convention(settings.layout.settingsDirectory.dir(resolvedRoot.absolutePath))

    settings.gradle.settingsEvaluated {
      val rootDir = extension.projectRoot.get().asFile

      // Configure catalogs
      for ((name, customPath) in extension.catalogsList) {
        val catalogFile: File = when (customPath) {
          null -> File(rootDir, "gradle/$name.versions.toml")
          is File -> if (customPath.isAbsolute) customPath else File(rootDir, customPath.path)
          is Path -> {
            val toFile = customPath.toFile()
            if (toFile.isAbsolute) toFile else File(rootDir, toFile.path)
          }
          is String -> {
            val fileObj = File(customPath)
            if (fileObj.isAbsolute) fileObj else File(rootDir, customPath)
          }
          else -> {
            val stringNotation = customPath.toString()
            val fileObj = File(stringNotation)
            if (fileObj.isAbsolute) fileObj else File(rootDir, stringNotation)
          }
        }

        if (catalogFile.exists()) {
          val defaultLibsFile = File(settings.settingsDir, "gradle/libs.versions.toml")
          if (name == "libs" && catalogFile.canonicalFile == defaultLibsFile.canonicalFile) {
            // Default libs catalog is already automatically loaded by Gradle from settingsDir/gradle/libs.versions.toml
            continue
          }
          val fileCollection = settings.layout.settingsDirectory.files(catalogFile)
          val existing = settings.dependencyResolutionManagement.versionCatalogs.findByName(name)
          if (existing != null) {
            existing.from(fileCollection)
          } else {
            settings.dependencyResolutionManagement.versionCatalogs.create(name) { catalog ->
              catalog.from(fileCollection)
            }
          }
        }
      }
    }
  }

  private fun resolveRoot(settings: Settings): File {
    // 1. Specific plugin root override
    System.getenv("VERSION_CATALOGS_BUILDER_ROOT")?.let { File(it) }?.takeIf { it.exists() }?.let { return it }

    // 2. Continuous integration / monorepo workspace override
    System.getenv("WORKSPACE_ROOT")?.let { File(it) }?.takeIf { it.exists() }?.let { return it }

    // 3. Upwards filesystem traversal for gradle/wrapper/gradle-wrapper.properties or .git
    var current: File? = settings.settingsDir
    while (current != null) {
      if (File(current, "gradle/wrapper/gradle-wrapper.properties").exists() || File(current, ".git").exists()) {
        return current
      }
      val parent = current.parentFile ?: break
      current = parent
    }

    // 4. Fallback to settings directory parent (if exists) or current directory
    return current ?: settings.settingsDir
  }
}
