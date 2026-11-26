package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.problems.Problems
import org.gradle.api.provider.MapProperty
import javax.inject.Inject

/**
 * Settings extension configuring workspace root directory and version catalog registrations
 * for [VersionCatalogsBuilderGradleSettingsPlugin].
 *
 * Backed by configuration-cache-compliant managed properties ([projectRoot] and [catalogs]).
 * Multiple calls registering the same catalog name will replace previous entries in [catalogs]
 * (retaining the last declared path) and emit an informational problem warning via [Problems].
 *
 * @param objects Gradle [ObjectFactory] used to instantiate managed properties.
 * @param problems Gradle [Problems] API service for diagnostic problem and warning reporting.
 */
@VersionCatalogsBuilderDsl
public open class VersionCatalogsBuilderGradleSettingsExtension @Inject constructor(
  objects: ObjectFactory,
  private val problems: Problems,
) {
  /**
   * Root directory of the workspace or project repository.
   *
   * Defaults to auto-discovery of the nearest parent directory containing
   * `gradle/wrapper/gradle-wrapper.properties` or `.git`.
   */
  public val projectRoot: DirectoryProperty = objects.directoryProperty()

  /**
   * Managed map of registered version catalog names to their backing [VersionCatalogIntention].
   */
  internal val catalogs: MapProperty<String, VersionCatalogIntention> =
    objects.mapProperty(String::class.java, VersionCatalogIntention::class.java)

  /**
   * Registers a version catalog by [name], defaulting to `gradle/{name}.versions.toml` relative to [projectRoot].
   *
   * Example:
   * ```kotlin
   * versionCatalogsBuilder {
   *   catalog("libs")
   * }
   * ```
   *
   * If called multiple times with the same [name], the subsequent call replaces earlier declarations
   * in the backing [catalogs] property and reports an informational warning.
   *
   * @param name Name of the version catalog accessor (e.g. `"libs"`, `"tools"`).
   */
  public fun catalog(name: String) {
    recordCatalog(name, null)
  }

  /**
   * Registers a version catalog by [name] with an explicit [from] path or file reference.
   *
   * Accepts any argument supported by Gradle file notation (`String`, `File`, `Path`, etc.).
   * Relative paths are resolved relative to [projectRoot].
   *
   * Example:
   * ```kotlin
   * versionCatalogsBuilder {
   *   catalog("tools", "gradle/tools.versions.toml")
   *   catalog("custom", file("config/custom.versions.toml"))
   * }
   * ```
   *
   * If called multiple times with the same [name], the subsequent call replaces earlier declarations
   * in the backing [catalogs] property and reports an informational warning.
   *
   * @param name Name of the version catalog accessor.
   * @param from File, relative path string, or notation pointing to the TOML catalog file.
   */
  public fun catalog(name: String, from: Any) {
    recordCatalog(name, from)
  }

  /**
   * Registers a version catalog by inferring its accessor name directly from [from].
   *
   * The catalog name is derived by stripping `.versions.toml` or `.toml` extensions from the file name
   * and converting kebab-case or snake_case separators to `lowerCamelCase`.
   *
   * Example:
   * ```kotlin
   * versionCatalogsBuilder {
   *   catalog("gradle/network-tools.versions.toml") // registers catalog accessor "networkTools"
   * }
   * ```
   *
   * @param from File, relative path string, or notation pointing to the TOML catalog file.
   */
  public fun catalog(from: Any) {
    val derivedName = from.derivedCatalogName
    recordCatalog(derivedName, from)
  }

  private fun recordCatalog(name: String, from: Any?) {
    val newIntention = VersionCatalogIntention(name = name, from = from, projectRoot = projectRoot)
    val existing = catalogs.getting(name).orNull

    if (existing != null) {
      val problemId = problems.groups.dependencies.declaration.problemId("Version catalog declaration overwritten")
      problems.reporter.report(problemId) { spec ->
        spec
          .contextualLabel("Version catalog '$name' declaration was overwritten")
          .details(existing replacedBy newIntention)
          .solution("Remove duplicate catalog('$name', ...) declarations or use distinct catalog names")
      }
    }

    catalogs.put(name, newIntention)
  }
}
