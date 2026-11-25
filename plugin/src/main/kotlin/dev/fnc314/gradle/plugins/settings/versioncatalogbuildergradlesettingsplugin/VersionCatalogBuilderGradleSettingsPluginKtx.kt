package dev.fnc314.gradle.plugins.settings.versioncatalogbuildergradlesettingsplugin

import org.gradle.api.initialization.Settings

/**
 * Accessor for [VersionCatalogBuilderGradleSettingsExtension] on [Settings].
 */
public val Settings.versionCatalogBuilder: VersionCatalogBuilderGradleSettingsExtension
  get() = extensions.getByType(VersionCatalogBuilderGradleSettingsExtension::class.java)
