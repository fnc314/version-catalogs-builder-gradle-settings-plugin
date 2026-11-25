package dev.fnc314.gradle.plugins.settings.versioncatalogbuildergradlesettingsplugin

import kotlin.test.Test
import kotlin.test.assertEquals

class VersionCatalogBuilderGradleSettingsPluginTest {
  @Test
  fun testPluginId() {
    val id = "dev.fnc314.gradle.plugins.settings.version-catalog-builder-gradle-settings-plugin"
    assertEquals("dev.fnc314.gradle.plugins.settings.version-catalog-builder-gradle-settings-plugin", id)
  }
}
