package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionCatalogIntentionTest {
  @Test
  fun testReportFormatting() {
    val project = ProjectBuilder.builder().build()
    val projectRootDir = File("/tmp/workspace-root")
    val projectRootProp = project.objects.directoryProperty().apply {
      set(projectRootDir)
    }

    val intention = VersionCatalogIntention(
      name = "networkTools",
      from = "gradle/network-tools.versions.toml",
      projectRoot = projectRootProp,
    )

    val expected = """
      Catalog Intention
       - name: networkTools
       - from: 'gradle/network-tools.versions.toml'
       - projectRoot: '/tmp/workspace-root'
       - resolved: '/tmp/workspace-root/gradle/network-tools.versions.toml'
    """.trimIndent()

    assertEquals(expected, intention.report())
  }

  @Test
  fun testReportFormattingDefaultLocation() {
    val project = ProjectBuilder.builder().build()
    val projectRootDir = File("/tmp/workspace-root")
    val projectRootProp = project.objects.directoryProperty().apply {
      set(projectRootDir)
    }

    val intention = VersionCatalogIntention(
      name = "libs",
      from = null,
      projectRoot = projectRootProp,
    )

    val expected = """
      Catalog Intention
       - name: libs
       - from: default location (gradle/libs.versions.toml)
       - projectRoot: '/tmp/workspace-root'
       - resolved: '/tmp/workspace-root/gradle/libs.versions.toml'
    """.trimIndent()

    assertEquals(expected, intention.report())
  }

  @Test
  fun testDeferredResolutionPicksUpLateProjectRootChange() {
    val project = ProjectBuilder.builder().build()
    val projectRootProp = project.objects.directoryProperty()

    val intention = VersionCatalogIntention(
      name = "tools",
      from = "gradle/tools.versions.toml",
      projectRoot = projectRootProp,
    )

    // Initially unset, deferred resolution does not fail or prematurely lock
    // Now set projectRoot afterwards:
    val targetDir = File("/custom/root")
    projectRootProp.set(targetDir)

    assertEquals(File(targetDir, "gradle/tools.versions.toml"), intention.targetFile)
  }
}
