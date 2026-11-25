package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public open class VersionCatalogsBuilderGradleSettingsExtension @Inject constructor(
  objects: ObjectFactory,
) {
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
    public fun register(name: String) {
      map[name] = null
    }

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
