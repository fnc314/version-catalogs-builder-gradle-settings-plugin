package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Settings extension configuring workspace root directory, version catalog registrations,
 * and tool build inclusions for [VersionCatalogsBuilderGradleSettingsPlugin].
 *
 * @param objects Gradle [ObjectFactory] used to instantiate managed properties.
 */
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

  internal val catalogsList: MutableMap<String, String?> = mutableMapOf()
  internal val toolBuildsList: MutableList<String> = mutableListOf()

  /**
   * Configures version catalogs to be registered within the settings dependency resolution management.
   *
   * Example:
   * ```kotlin
   * catalogs {
   *   register("libs")
   *   register("local", "gradle/local.versions.toml")
   * }
   * ```
   *
   * @param action Action configuring the [CatalogsDsl] builder.
   */
  public fun catalogs(action: Action<CatalogsDsl>) {
    val dsl = CatalogsDsl(catalogsList)
    action.execute(dsl)
  }

  /**
   * Configures composite tool builds to be included via `pluginManagement.includeBuild(...)`.
   *
   * Example:
   * ```kotlin
   * toolBuilds {
   *   include("build-logic")
   * }
   * ```
   *
   * @param action Action configuring the [ToolBuildsDsl] builder.
   */
  public fun toolBuilds(action: Action<ToolBuildsDsl>) {
    val dsl = ToolBuildsDsl(toolBuildsList)
    action.execute(dsl)
  }

  /**
   * DSL builder for registering version catalog definitions.
   *
   * @property map Internal mutable map storing catalog names mapped to optional relative file paths.
   */
  public class CatalogsDsl(private val map: MutableMap<String, String?>) {
    /**
     * Registers a version catalog by [name], defaulting to `gradle/{name}.versions.toml` relative to [projectRoot].
     *
     * @param name Name of the version catalog accessor (e.g. `"libs"`, `"tools"`).
     */
    public fun register(name: String) {
      map[name] = null
    }

    /**
     * Registers a version catalog by [name] with an explicit relative [path] to its TOML file.
     *
     * @param name Name of the version catalog accessor.
     * @param path Relative path to the TOML catalog file from [projectRoot].
     */
    public fun register(name: String, path: String) {
      map[name] = path
    }
  }

  /**
   * DSL builder for declaring tool builds included via `pluginManagement.includeBuild`.
   *
   * @property list Internal mutable list storing relative paths of tool builds.
   */
  public class ToolBuildsDsl(private val list: MutableList<String>) {
    /**
     * Declares a relative directory path to include as an included tool build.
     *
     * @param relativePath Relative directory path from [projectRoot] containing an included build.
     */
    public fun include(relativePath: String) {
      list.add(relativePath)
    }
  }
}
