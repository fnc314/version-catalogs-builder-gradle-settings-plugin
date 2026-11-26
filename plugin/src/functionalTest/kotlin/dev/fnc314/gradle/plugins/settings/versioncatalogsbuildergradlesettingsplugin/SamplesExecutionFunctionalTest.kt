package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class SamplesExecutionFunctionalTest {

  @Test
  fun testBasicMultiCatalogSample() {
    val sampleDir = File("../samples/basic-multi-catalog")
    if (!sampleDir.exists()) return

    // Inject pluginClasspath into sample settings for local functional testing
    val testDir = File("build/samplesTest/basic-multi-catalog").apply {
      deleteRecursively()
      mkdirs()
    }
    sampleDir.copyRecursively(testDir, overwrite = true)

    val settingsFile = File(testDir, "settings.gradle.kts")
    val content = settingsFile.readText()
    // Replace plugin version with current classpath plugin
    val modified = content.replace(Regex("""id\("dev\.fnc314\.gradle\.plugins\.settings\.version-catalogs-builder-gradle-settings-plugin"\) version ".*""""), """id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin")""")
    settingsFile.writeText(modified)

    val result = GradleRunner.create()
      .withProjectDir(testDir)
      .withEnvironment(mapOf("VERSION_CATALOGS_BUILDER_ROOT" to testDir.absolutePath))
      .withPluginClasspath()
      .withArguments("buildAll", "--info")
      .forwardOutput()
      .build()

    assertTrue(result.output.contains("All subprojects evaluated successfully with multiple version catalogs!"))
  }

  @Test
  fun testCompositeToolBuildsSample() {
    val sampleDir = File("../samples/composite-tool-builds")
    if (!sampleDir.exists()) return

    val testDir = File("build/samplesTest/composite-tool-builds").apply {
      deleteRecursively()
      mkdirs()
    }
    sampleDir.copyRecursively(testDir, overwrite = true)

    val settingsFile = File(testDir, "settings.gradle.kts")
    val content = settingsFile.readText()
    val modified = content.replace(Regex("""id\("dev\.fnc314\.gradle\.plugins\.settings\.version-catalogs-builder-gradle-settings-plugin"\) version ".*""""), """id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin")""")
    settingsFile.writeText(modified)

    val result = GradleRunner.create()
      .withProjectDir(testDir)
      .withEnvironment(mapOf("VERSION_CATALOGS_BUILDER_ROOT" to testDir.absolutePath))
      .withPluginClasspath()
      .withArguments(":library:greeting", "--info")
      .forwardOutput()
      .build()

    assertTrue(result.output.contains("Hello from build-logic convention plugin in :library!"))
  }

  @Test
  fun testCustomRootMonorepoSample() {
    val sampleDir = File("../samples/custom-root-monorepo")
    if (!sampleDir.exists()) return

    val testDir = File("build/samplesTest/custom-root-monorepo").apply {
      deleteRecursively()
      mkdirs()
    }
    sampleDir.copyRecursively(testDir, overwrite = true)

    val apiDir = File(testDir, "packages/service-api")
    val settingsFile = File(apiDir, "settings.gradle.kts")
    val content = settingsFile.readText()
    val modified = content.replace(Regex("""id\("dev\.fnc314\.gradle\.plugins\.settings\.version-catalogs-builder-gradle-settings-plugin"\) version ".*""""), """id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin")""")
    settingsFile.writeText(modified)

    val result = GradleRunner.create()
      .withProjectDir(apiDir)
      .withEnvironment(mapOf("VERSION_CATALOGS_BUILDER_ROOT" to testDir.absolutePath))
      .withPluginClasspath()
      .withArguments("help", "--info")
      .forwardOutput()
      .build()

    assertTrue(result.output.contains("BUILD SUCCESSFUL"))
  }
}
