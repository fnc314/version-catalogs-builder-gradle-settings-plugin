package dev.fnc314.gradle.plugins.settings.versioncatalogbuildergradlesettingsplugin

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public open class VersionCatalogBuilderGradleSettingsExtension @Inject constructor(
  objects: ObjectFactory,
) {
  /**
   * Root directory of the repository/workspace.
   *
   * If unset, auto-resolved via:
   * 1. GRADLE_DOCS_ROOT env var
   * 2. WORKSPACE_ROOT env var
   * 3. Upward filesystem search for gradle/wrapper/gradle-wrapper.properties
   * 4. Upward filesystem search for settings.gradle / settings.gradle.kts or .git
   */
  public val projectRoot: DirectoryProperty = objects.directoryProperty()

  internal val catalogsList: MutableMap<String, String?> = mutableMapOf()
  internal val toolBuildsList: MutableList<String> = mutableListOf()

  public fun catalogs(action: Action<CatalogsDsl>) {
    val dsl = CatalogsDsl(catalogsList)
    action.execute(dsl)
  }

  public fun toolBuilds(action: Action<ToolBuildsDsl>) {
    val dsl = ToolBuildsDsl(toolBuildsList)
    action.execute(dsl)
  }

  public class CatalogsDsl(private val map: MutableMap<String, String?>) {
    /**
     * Registers a catalog name.
     * Abbreviated syntax: register("libs") implicitly maps to "gradle/libs.versions.toml".
     */
    public fun register(name: String) {
      map[name] = null
    }

    /**
     * Registers a catalog name with a custom relative path.
     */
    public fun register(name: String, path: String) {
      map[name] = path
    }
  }

  public class ToolBuildsDsl(private val list: MutableList<String>) {
    public fun include(relativePath: String) {
      list.add(relativePath)
    }
  }
}
