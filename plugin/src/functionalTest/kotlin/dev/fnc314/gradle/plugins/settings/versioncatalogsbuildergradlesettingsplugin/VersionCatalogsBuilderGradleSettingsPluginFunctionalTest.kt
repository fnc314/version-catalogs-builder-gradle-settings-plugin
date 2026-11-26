package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class VersionCatalogsBuilderGradleSettingsPluginFunctionalTest {
  @Test
  fun testApplyPluginInSettings() {
    val projectDir = File("build/functionalTestProject").apply {
      deleteRecursively()
      mkdirs()
    }

    val gradleWrapperProps = File(projectDir, "gradle/wrapper").apply { mkdirs() }
    File(gradleWrapperProps, "gradle-wrapper.properties").writeText("distributionUrl=fake")

    val settingsFile = File(projectDir, "settings.gradle.kts")
    settingsFile.writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin")
      }
      versionCatalogsBuilder {
        catalog("libs")
      }
      rootProject.name = "test-functional-version-catalogs-builder"
      """.trimIndent(),
    )

    val buildFile = File(projectDir, "build.gradle.kts")
    buildFile.writeText("""
      tasks.register("testTask") {
        doLast {
          println("Version catalogs builder settings functional test successful")
        }
      }
    """.trimIndent())

    val result = GradleRunner.create()
      .withProjectDir(projectDir)
      .withPluginClasspath()
      .withArguments("testTask", "-q")
      .forwardOutput()
      .build()

    assertTrue(result.output.contains("Version catalogs builder settings functional test successful"))
  }

  @Test
  fun testMisalignedLibsCatalogThrowsError() {
    val projectDir = File("build/functionalTestMisalignedLibs").apply {
      deleteRecursively()
      mkdirs()
    }

    val gradleWrapperProps = File(projectDir, "gradle/wrapper").apply { mkdirs() }
    File(gradleWrapperProps, "gradle-wrapper.properties").writeText("distributionUrl=fake")

    // Create local gradle/libs.versions.toml so Gradle registers 'libs' by default
    val localGradleDir = File(projectDir, "gradle").apply { mkdirs() }
    File(localGradleDir, "libs.versions.toml").writeText("""
      [versions]
      kotlin = "2.0.0"
    """.trimIndent())

    // Create custom foreign catalog file
    val foreignDir = File(projectDir, "custom").apply { mkdirs() }
    File(foreignDir, "other.versions.toml").writeText("""
      [versions]
      kotlin = "2.1.0"
    """.trimIndent())

    val settingsFile = File(projectDir, "settings.gradle.kts")
    settingsFile.writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin")
      }
      versionCatalogsBuilder {
        // Pointing 'libs' to a conflicting custom file when local libs.versions.toml exists
        catalog("libs", "custom/other.versions.toml")
      }
      rootProject.name = "test-functional-misaligned-libs"
      """.trimIndent(),
    )

    val buildFile = File(projectDir, "build.gradle.kts")
    buildFile.writeText("""
      tasks.register("testTask")
    """.trimIndent())

    val result = GradleRunner.create()
      .withProjectDir(projectDir)
      .withPluginClasspath()
      .withArguments("testTask")
      .forwardOutput()
      .buildAndFail()

    assertTrue(result.output.contains("conflicts with requested catalog file"))
  }
}
