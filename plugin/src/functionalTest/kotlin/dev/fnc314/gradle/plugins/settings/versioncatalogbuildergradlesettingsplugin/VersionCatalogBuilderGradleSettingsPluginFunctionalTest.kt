package dev.fnc314.gradle.plugins.settings.versioncatalogbuildergradlesettingsplugin

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class VersionCatalogBuilderGradleSettingsPluginFunctionalTest {
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
        id("dev.fnc314.gradle.plugins.settings.version-catalog-builder-gradle-settings-plugin")
      }
      versionCatalogBuilder {
        catalogs {
          register("libs")
        }
      }
      rootProject.name = "test-functional-version-catalog-builder"
      """.trimIndent(),
    )

    val buildFile = File(projectDir, "build.gradle.kts")
    buildFile.writeText("""
      tasks.register("testTask") {
        doLast {
          println("Version catalog builder settings functional test successful")
        }
      }
    """.trimIndent())

    val result = GradleRunner.create()
      .withProjectDir(projectDir)
      .withPluginClasspath()
      .withArguments("testTask", "-q")
      .forwardOutput()
      .build()

    assertTrue(result.output.contains("Version catalog builder settings functional test successful"))
  }
}
