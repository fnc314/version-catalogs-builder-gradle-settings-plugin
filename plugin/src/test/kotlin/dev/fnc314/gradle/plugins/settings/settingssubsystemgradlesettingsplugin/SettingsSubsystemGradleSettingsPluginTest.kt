package dev.fnc314.gradle.plugins.settings.settingssubsystemgradlesettingsplugin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SettingsSubsystemGradleSettingsPluginTest {
  @Test
  fun testPluginId() {
    val id = "dev.fnc314.gradle.plugins.settings.settings-subsystem-gradle-settings-plugin"
    assertEquals("dev.fnc314.gradle.plugins.settings.settings-subsystem-gradle-settings-plugin", id)
  }
}
