package dev.fnc314.gradle.generators

import java.io.File

/**
 * Supported sample project types for the version-catalogs-builder plugin.
 */
public enum class SampleType(public val slug: String, public val displayName: String) {
  /**
   * Sample demonstrating multiple version catalogs with standard and custom file paths.
   */
  BASIC_MULTI_CATALOG("basic-multi-catalog", "Basic Multi-Catalog Setup"),

  /**
   * Sample demonstrating composite tool build inclusion via toolBuilds block.
   */
  COMPOSITE_TOOL_BUILDS("composite-tool-builds", "Composite Tool Builds"),

  /**
   * Sample demonstrating monorepo directory layout with nested sub-builds and scoped mise environment variables.
   */
  CUSTOM_ROOT_MONOREPO("custom-root-monorepo", "Custom Root Monorepo");

  public companion object {
    /**
     * Resolves a [SampleType] by matching against slug or enum name (case-insensitive).
     *
     * @param input Raw slug or name input.
     * @return Matching [SampleType], or null if unrecognised.
     */
    public fun fromSlug(input: String): SampleType? {
      val normalized = input.trim().lowercase().replace('_', '-')
      return entries.firstOrNull { it.slug == normalized || it.name.lowercase().replace('_', '-') == normalized }
    }
  }
}

/**
 * Generator engine creating runnable sample Gradle projects demonstrating canonical use cases
 * of `version-catalogs-builder-gradle-settings-plugin`.
 */
public object SampleProjectGenerator {

  /**
   * Default version of the plugin applied in generated samples.
   */
  public const val DEFAULT_PLUGIN_VERSION: String = "2.0.3"

  /**
   * Generates a sample project of the specified [type] inside [destinationDir].
   *
   * @param type The [SampleType] describing the architecture and features to scaffold.
   * @param destinationDir Target directory where the project will be generated.
   * @param pluginVersion Version string of the plugin to configure in settings.
   */
  public fun generate(
    type: SampleType,
    destinationDir: File,
    pluginVersion: String = DEFAULT_PLUGIN_VERSION,
  ) {
    destinationDir.mkdirs()
    when (type) {
      SampleType.BASIC_MULTI_CATALOG -> generateBasicMultiCatalog(destinationDir, pluginVersion)
      SampleType.COMPOSITE_TOOL_BUILDS -> generateCompositeToolBuilds(destinationDir, pluginVersion)
      SampleType.CUSTOM_ROOT_MONOREPO -> generateCustomRootMonorepo(destinationDir, pluginVersion)
    }
  }

  private fun generateBasicMultiCatalog(dest: File, pluginVersion: String) {
    // 1. Root settings.gradle.kts
    File(dest, "settings.gradle.kts").writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "$pluginVersion"
      }

      versionCatalogsBuilder {
        catalogs {
          // Standard catalog mapping to gradle/libs.versions.toml
          register("libs")
          // Custom catalog mapping to an explicit file path
          register("tools", "gradle/tools.versions.toml")
        }
      }

      rootProject.name = "sample-basic-multi-catalog"

      include(":app")
      include(":core")
      """.trimIndent() + "\n",
    )

    // 2. Root build.gradle.kts
    File(dest, "build.gradle.kts").writeText(
      """
      plugins {
        base
      }

      tasks.register("buildAll") {
        dependsOn(subprojects.map { it.tasks.named("help") })
        doLast {
          println("All subprojects evaluated successfully with multiple version catalogs!")
        }
      }
      """.trimIndent() + "\n",
    )

    // 3. Catalogs
    val gradleDir = File(dest, "gradle").apply { mkdirs() }
    File(gradleDir, "libs.versions.toml").writeText(
      """
      [versions]
      kotlin = "2.2.0"
      coroutines = "1.9.0"

      [libraries]
      coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
      """.trimIndent() + "\n",
    )

    File(gradleDir, "tools.versions.toml").writeText(
      """
      [versions]
      detekt = "1.23.6"
      ktlint = "1.3.1"

      [libraries]
      detekt-cli = { module = "io.gitlab.arturbosch.detekt:detekt-cli", version.ref = "detekt" }
      """.trimIndent() + "\n",
    )

    // 4. Subprojects
    val appDir = File(dest, "app").apply { mkdirs() }
    File(appDir, "build.gradle.kts").writeText(
      """
      plugins {
        `java-library`
      }

      dependencies {
        // Consuming from libs catalog
        implementation(libs.coroutines.core)
        // Consuming from tools catalog
        compileOnly(tools.detekt.cli)
      }
      """.trimIndent() + "\n",
    )

    val coreDir = File(dest, "core").apply { mkdirs() }
    File(coreDir, "build.gradle.kts").writeText(
      """
      plugins {
        `java-library`
      }

      dependencies {
        implementation(libs.coroutines.core)
      }
      """.trimIndent() + "\n",
    )

    // 5. README.md
    File(dest, "README.md").writeText(
      """
      # Sample: Basic Multi-Catalog

      This sample demonstrates how to configure and resolve multiple version catalogs using `version-catalogs-builder-gradle-settings-plugin`:
      - **Abbreviated syntax**: `register("libs")` resolves automatically to `gradle/libs.versions.toml`.
      - **Explicit path syntax**: `register("tools", "gradle/tools.versions.toml")` resolves to a customized catalog file.

      ## Project Structure

      ```
      .
      ├── build.gradle.kts
      ├── settings.gradle.kts
      ├── gradle/
      │   ├── libs.versions.toml    # Standard libraries catalog (exposed as 'libs')
      │   └── tools.versions.toml   # Custom developer tools catalog (exposed as 'tools')
      ├── app/
      │   └── build.gradle.kts      # Consumes both libs and tools accessors
      └── core/
          └── build.gradle.kts      # Consumes libs accessor
      ```

      ## Verifying the Build

      Run the following commands inside this directory:
      ```bash
      ./gradlew help
      ./gradlew :app:dependencies
      ```
      """.trimIndent() + "\n",
    )
  }

  private fun generateCompositeToolBuilds(dest: File, pluginVersion: String) {
    // 1. Root settings.gradle.kts
    File(dest, "settings.gradle.kts").writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "$pluginVersion"
      }

      versionCatalogsBuilder {
        catalogs {
          register("libs")
        }
        toolBuilds {
          // Include custom convention plugin build
          include("build-logic")
        }
      }

      rootProject.name = "sample-composite-tool-builds"

      include(":library")
      """.trimIndent() + "\n",
    )

    // 2. Root build.gradle.kts
    File(dest, "build.gradle.kts").writeText(
      """
      plugins {
        base
      }
      """.trimIndent() + "\n",
    )

    // 3. Catalogs
    val gradleDir = File(dest, "gradle").apply { mkdirs() }
    File(gradleDir, "libs.versions.toml").writeText(
      """
      [versions]
      kotlin = "2.2.0"
      """.trimIndent() + "\n",
    )

    // 4. Included build: build-logic
    val buildLogicDir = File(dest, "build-logic").apply { mkdirs() }
    File(buildLogicDir, "settings.gradle.kts").writeText(
      """
      rootProject.name = "sample-build-logic"
      """.trimIndent() + "\n",
    )
    File(buildLogicDir, "build.gradle.kts").writeText(
      """
      plugins {
        `kotlin-dsl`
      }

      repositories {
        mavenCentral()
        gradlePluginPortal()
      }

      gradlePlugin {
        plugins {
          register("sampleConvention") {
            id = "sample.conventions"
            implementationClass = "sample.SampleConventionPlugin"
          }
        }
      }
      """.trimIndent() + "\n",
    )
    val pluginSrc = File(buildLogicDir, "src/main/kotlin/sample").apply { mkdirs() }
    File(pluginSrc, "SampleConventionPlugin.kt").writeText(
      """
      package sample

      import org.gradle.api.Plugin
      import org.gradle.api.Project

      class SampleConventionPlugin : Plugin<Project> {
        override fun apply(target: Project) {
          target.tasks.register("greeting") {
            doLast {
              println("Hello from build-logic convention plugin in " + target.path + "!")
            }
          }
        }
      }
      """.trimIndent() + "\n",
    )

    // 5. Consumer subproject
    val libDir = File(dest, "library").apply { mkdirs() }
    File(libDir, "build.gradle.kts").writeText(
      """
      plugins {
        id("sample.conventions")
      }
      """.trimIndent() + "\n",
    )

    // 6. README.md
    File(dest, "README.md").writeText(
      """
      # Sample: Composite Tool Builds

      This sample demonstrates how to include composite tool builds (such as `build-logic`) using `toolBuilds { include("build-logic") }` in `version-catalogs-builder-gradle-settings-plugin`.

      ## Project Structure

      ```
      .
      ├── build.gradle.kts
      ├── settings.gradle.kts
      ├── gradle/
      │   └── libs.versions.toml    # Version catalog
      ├── build-logic/              # Included tool build providing convention plugins
      │   ├── build.gradle.kts
      │   └── settings.gradle.kts
      └── library/
          └── build.gradle.kts      # Applies 'sample.conventions' plugin from build-logic
      ```

      ## Verifying the Build

      Run the task defined by the included tool build plugin:
      ```bash
      ./gradlew :library:greeting
      ```
      Output:
      ```
      Hello from build-logic convention plugin in :library!
      ```
      """.trimIndent() + "\n",
    )
  }

  private fun generateCustomRootMonorepo(dest: File, pluginVersion: String) {
    // 1. Root shared directory
    val rootGradle = File(dest, "gradle").apply { mkdirs() }
    File(rootGradle, "libs.versions.toml").writeText(
      """
      [versions]
      guava = "33.2.1-jre"

      [libraries]
      guava = { module = "com.google.guava:guava", version.ref = "guava" }
      """.trimIndent() + "\n",
    )

    // Marker file representing workspace boundary (.git or wrapper)
    val rootWrapper = File(dest, "gradle/wrapper").apply { mkdirs() }
    File(rootWrapper, "gradle-wrapper.properties").writeText(
      "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.9.2-bin.zip\n",
    )

    // 2. Scoped mise.toml
    File(dest, "mise.toml").writeText(
      """
      [env]
        # Scoped environment variable pointing to the monorepo root.
        # This allows nested builds to resolve the workspace root without polluting global shell state.
        VERSION_CATALOGS_BUILDER_ROOT = "{{config_root}}"
        WORKSPACE_ROOT = "{{config_root}}"
      """.trimIndent() + "\n",
    )

    // 3. Nested service: packages/service-api
    val apiDir = File(dest, "packages/service-api").apply { mkdirs() }
    File(apiDir, "settings.gradle.kts").writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "$pluginVersion"
      }

      versionCatalogsBuilder {
        catalogs {
          // Discovers parent workspace root and loads gradle/libs.versions.toml
          register("libs")
        }
      }

      rootProject.name = "service-api"
      """.trimIndent() + "\n",
    )
    File(apiDir, "build.gradle.kts").writeText(
      """
      plugins {
        `java-library`
      }

      dependencies {
        implementation(libs.guava)
      }
      """.trimIndent() + "\n",
    )

    // 4. Nested service: packages/service-worker
    val workerDir = File(dest, "packages/service-worker").apply { mkdirs() }
    File(workerDir, "settings.gradle.kts").writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.version-catalogs-builder-gradle-settings-plugin") version "$pluginVersion"
      }

      versionCatalogsBuilder {
        catalogs {
          register("libs")
        }
      }

      rootProject.name = "service-worker"
      """.trimIndent() + "\n",
    )
    File(workerDir, "build.gradle.kts").writeText(
      """
      plugins {
        `java-library`
      }

      dependencies {
        implementation(libs.guava)
      }
      """.trimIndent() + "\n",
    )

    // 5. README.md
    File(dest, "README.md").writeText(
      """
      # Sample: Custom Root Monorepo

      This sample demonstrates how nested sub-builds in a monorepo or repository workspace automatically discover the centralized `gradle/libs.versions.toml` located in the root repository directory.

      ## Scoped Environment Variables with `mise`

      To avoid polluting developer or CI shell environments, this sample includes a `mise.toml` file that establishes scoped environment variables:
      ```toml
      [env]
        VERSION_CATALOGS_BUILDER_ROOT = "{{config_root}}"
        WORKSPACE_ROOT = "{{config_root}}"
      ```
      When inside this directory or its subdirectories, `mise` activates these variables locally, directing the plugin to the sample root.

      ## Project Structure

      ```
      .
      ├── mise.toml                  # Scoped environment configuration
      ├── gradle/
      │   ├── libs.versions.toml     # Shared root version catalog
      │   └── wrapper/
      │       └── gradle-wrapper.properties  # Marker file for workspace root discovery
      └── packages/
          ├── service-api/
          │   ├── build.gradle.kts
          │   └── settings.gradle.kts  # Discovers root & consumes shared catalog
          └── service-worker/
              ├── build.gradle.kts
              └── settings.gradle.kts  # Discovers root & consumes shared catalog
      ```

      ## Verifying the Build

      From within a nested package directory:
      ```bash
      cd packages/service-api
      ./gradlew help
      ./gradlew dependencies
      ```
      """.trimIndent() + "\n",
    )
  }
}
