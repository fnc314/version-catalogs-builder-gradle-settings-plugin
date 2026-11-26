package dev.fnc314.gradle.generators

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task generating all canonical version-catalogs-builder sample projects into the samples/ directory.
 */
public abstract class GenerateAllSamplesTask : DefaultTask() {

  /**
   * Output directory where samples will be created.
   */
  @get:OutputDirectory
  public abstract val samplesDirectory: DirectoryProperty

  init {
    group = "generation"
    description = "Generates all canonical sample projects in samples/"
  }

  @TaskAction
  public fun generateAll() {
    val baseDir = samplesDirectory.asFile.get()
    for (sampleType in SampleType.entries) {
      val dest = java.io.File(baseDir, sampleType.slug)
      logger.lifecycle("Generating sample '${sampleType.displayName}' in ${dest.absolutePath}...")
      SampleProjectGenerator.generate(sampleType, dest)
    }
    logger.lifecycle("All samples generated successfully.")
  }
}
