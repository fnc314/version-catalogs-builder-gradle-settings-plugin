package dev.fnc314.gradle.generators

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Gradle task generating sample projects demonstrating canonical usage of version-catalogs-builder.
 */
public abstract class GenerateSampleTask : DefaultTask() {

  /**
   * Slug or name of the sample project to generate (e.g. `basic-multi-catalog`, `multi-project-shared-catalog`, `custom-root-monorepo`).
   */
  @get:Input
  @get:Option(option = "sampleType", description = "Sample type: basic-multi-catalog, multi-project-shared-catalog, or custom-root-monorepo")
  public abstract val sampleType: Property<String>

  /**
   * Target directory where the sample project will be generated.
   */
  @get:OutputDirectory
  public abstract val destinationDir: DirectoryProperty

  /**
   * Optional plugin version override. Defaults to the current project version.
   */
  @get:Input
  @get:Optional
  @get:Option(option = "pluginVersion", description = "Plugin version to configure in the generated sample")
  public abstract val pluginVersion: Property<String>

  init {
    group = "generation"
    description = "Generates a version-catalogs-builder sample project"
    sampleType.convention("basic-multi-catalog")
  }

  @TaskAction
  public fun generate() {
    val typeSlug = sampleType.get()
    val type = SampleType.fromSlug(typeSlug)
      ?: throw IllegalArgumentException("Unknown sample type '$typeSlug'. Valid types: ${SampleType.entries.joinToString { it.slug }}")

    val dest = destinationDir.asFile.get()
    val version = pluginVersion.get()

    logger.lifecycle("Generating sample '${type.displayName}' ($typeSlug) in ${dest.absolutePath} (plugin version $version)...")
    SampleProjectGenerator.generate(type, dest, version)
    logger.lifecycle("Sample generated successfully.")
  }
}
