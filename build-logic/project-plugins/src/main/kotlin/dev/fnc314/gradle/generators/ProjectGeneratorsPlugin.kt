package dev.fnc314.gradle.generators

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Convention plugin registering generator tasks for version-catalogs-builder.
 */
public class ProjectGeneratorsPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.tasks.register<GenerateSampleTask>("generateSample") {
      destinationDir.convention(
        target.layout.projectDirectory.dir(
          sampleType.map { "samples/$it" }
        )
      )
    }

    target.tasks.register<GenerateAllSamplesTask>("generateAllSamples") {
      samplesDirectory.convention(target.layout.projectDirectory.dir("samples"))
    }
  }
}
