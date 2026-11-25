package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.initialization.Settings

/**
 * Accessor for [VersionCatalogsBuilderGradleSettingsExtension] on [Settings].
 */
public val Settings.versionCatalogsBuilder: VersionCatalogsBuilderGradleSettingsExtension
  get() = extensions.getByType(VersionCatalogsBuilderGradleSettingsExtension::class.java)
