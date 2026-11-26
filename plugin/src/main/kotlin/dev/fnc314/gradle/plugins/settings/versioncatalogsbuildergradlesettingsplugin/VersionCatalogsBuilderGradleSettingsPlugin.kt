package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.problems.Problems
import java.io.File
import javax.inject.Inject

/**
 * Settings convention plugin orchestrating workspace root discovery and version catalog registration.
 *
 * @param problems Gradle [Problems] API service for diagnostic problem and warning reporting.
 */
public abstract class VersionCatalogsBuilderGradleSettingsPlugin @Inject constructor(
  private val problems: Problems,
) : Plugin<Settings> {
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
      val catalogMap = extension.catalogs.get()

      // Configure catalogs
      for ((name, customPath) in catalogMap) {
        val relPath = if (customPath.isNotEmpty()) customPath else "gradle/$name.versions.toml"
        val candidate = File(relPath)
        val catalogFile = if (candidate.isAbsolute) candidate else File(rootDir, relPath)

        if (!catalogFile.exists()) continue

        val defaultLibsFile = File(settings.settingsDir, "gradle/libs.versions.toml")
        val isDefaultLibs = name == "libs" && defaultLibsFile.exists() && catalogFile.canonicalFile == defaultLibsFile.canonicalFile

        if (isDefaultLibs) {
          // Case A: Default libs catalog is already automatically loaded by Gradle from settingsDir/gradle/libs.versions.toml
          continue
        }

        if (name == "libs" && defaultLibsFile.exists()) {
          // Case B: Conflicting libs catalog registration (e.g. custom path for libs while local gradle/libs.versions.toml exists)
          val problemId = problems.groups.dependencies.declaration.problemId("Conflicting version catalog file")
          throw problems.reporter.throwing(
            IllegalStateException(
              "Version catalog '$name' is already registered by Gradle from '${defaultLibsFile.path}', " +
                "which conflicts with requested catalog file '${catalogFile.path}'. " +
                "Remove the local gradle/libs.versions.toml or use a distinct catalog name."
            ),
            problemId,
          ) { spec ->
            spec
              .contextualLabel("Version catalog '$name' conflicts with existing default catalog")
              .details(
                "Version catalog '$name' is registered by Gradle from '${defaultLibsFile.path}', " +
                  "conflicting with requested catalog file '${catalogFile.path}'."
              )
              .solution("Remove the local gradle/libs.versions.toml or use a distinct catalog name")
          }
        }

        val existing = settings.dependencyResolutionManagement.versionCatalogs.findByName(name)
        if (existing != null) {
          // Case C: Duplicate registration on a custom catalog name
          val problemId = problems.groups.dependencies.declaration.problemId("Duplicate version catalog declaration")
          throw problems.reporter.throwing(
            IllegalStateException(
              "Version catalog '$name' is already registered, " +
                "which conflicts with requested catalog file '${catalogFile.path}'."
            ),
            problemId,
          ) { spec ->
            spec
              .contextualLabel("Version catalog '$name' is already registered")
              .details("Version catalog '$name' conflicts with requested catalog file '${catalogFile.path}'.")
              .solution("Use a distinct catalog name for each version catalog")
          }
        }

        // Case D: Register new catalog cleanly
        val fileCollection = settings.layout.settingsDirectory.files(catalogFile)
        settings.dependencyResolutionManagement.versionCatalogs.create(name) { catalog ->
          catalog.from(fileCollection)
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
