package dev.fnc314.gradle.plugins.settings.settingssubsystemgradlesettingsplugin

import org.gradle.api.initialization.Settings

/**
 * Accessor for [SettingsSubsystemGradleSettingsExtension] on [Settings].
 */
public val Settings.subsystemSettings: SettingsSubsystemGradleSettingsExtension
  get() = extensions.getByType(SettingsSubsystemGradleSettingsExtension::class.java)

public val Settings.settingsSubsystem: SettingsSubsystemGradleSettingsExtension
  get() = extensions.getByType(SettingsSubsystemGradleSettingsExtension::class.java)
