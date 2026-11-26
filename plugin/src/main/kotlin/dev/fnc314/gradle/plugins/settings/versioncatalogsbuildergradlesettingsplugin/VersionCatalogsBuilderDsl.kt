package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

/**
 * DSL marker annotation for Gradle Settings version catalog builder configuration.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
@Retention(AnnotationRetention.BINARY)
public annotation class VersionCatalogsBuilderDsl
