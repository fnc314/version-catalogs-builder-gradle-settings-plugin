package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import kotlin.test.Test
import kotlin.test.assertEquals

class VersionCatalogsBuilderGradleSettingsPluginTest {
  @Test
  fun testPluginId() {
    val id = "dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin"
    assertEquals("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin", id)
  }
}
