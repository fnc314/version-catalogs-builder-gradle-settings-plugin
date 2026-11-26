package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Settings extension configuring workspace root directory and version catalog registrations
 * for [VersionCatalogsBuilderGradleSettingsPlugin].
 *
 * @param objects Gradle [ObjectFactory] used to instantiate managed properties.
 */
@VersionCatalogsBuilderDsl
public open class VersionCatalogsBuilderGradleSettingsExtension @Inject constructor(
  objects: ObjectFactory,
) {
  /**
   * Root directory of the workspace or project repository.
   *
   * Defaults to auto-discovery of the nearest parent directory containing
   * `gradle/wrapper/gradle-wrapper.properties` or `.git`.
   */
  public val projectRoot: DirectoryProperty = objects.directoryProperty()

  internal val catalogsList: MutableMap<String, Any?> = mutableMapOf()

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
   * @param name Name of the version catalog accessor (e.g. `"libs"`, `"tools"`).
   */
  public fun catalog(name: String) {
    catalogsList[name] = null
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
   * @param name Name of the version catalog accessor.
   * @param from File, relative path string, or notation pointing to the TOML catalog file.
   */
  public fun catalog(name: String, from: Any) {
    catalogsList[name] = from
  }
}
