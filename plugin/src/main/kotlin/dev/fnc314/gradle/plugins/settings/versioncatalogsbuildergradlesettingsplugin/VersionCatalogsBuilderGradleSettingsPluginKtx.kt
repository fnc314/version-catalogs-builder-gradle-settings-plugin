package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.initialization.Settings

/**
 * Type-safe accessor for [VersionCatalogsBuilderGradleSettingsExtension] on Gradle [Settings].
 */
public val Settings.versionCatalogsBuilder: VersionCatalogsBuilderGradleSettingsExtension
  get() = extensions.getByType(VersionCatalogsBuilderGradleSettingsExtension::class.java)

/** The name of the [VersionCatalogsBuilderGradleSettingsExtension] extension. */
internal const val VERSION_CATALOGS_BUILDER_EXTENSION_NAME: String = "versionCatalogsBuilder"
