@file:[
  Suppress("UnstableApiUsage")
  OptIn(
    ExperimentalKotlinGradlePluginApi::class,
    InternalKotlinGradlePluginApi::class,
  )
]

import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.projectextensions.defaultConfigs
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.projectextensions.kotlinVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  // Apply the Kotlin JVM plugin to add support for Kotlin.
  alias(libs.plugins.kotlin.jvm)

  // Dokka
  alias(libs.plugins.dokka.config)

  // Publishing (and Signing)
  alias(libs.plugins.publishing)

  alias(libs.plugins.autonomousapps.dependency.analysis)
}

version = libs.versions.project.get()
group = "dev.fnc314.gradle.plugins.settings"

kotlin {
  explicitApi()
  jvmToolchain(
    libs.versions.jdk.map { it.toInt() }.get()
  )
  compilerOptions {
    jvmTarget = libs.versions.jdk.map { JvmTarget.fromTarget(it) }
    apiVersion = KotlinVersion.fromVersion(kotlinVersion)
    languageVersion = KotlinVersion.fromVersion(kotlinVersion)
    verbose = true
    optIn.addAll(
      "kotlin.ExperimentalStdlibApi",
    )
    jvmDefault = JvmDefaultMode.ENABLE
  }
}

java {
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    register<MavenPublication>(name = "gpr") {
      from(components["kotlin"])
      artifact(tasks.named("dokkaHtmlJar"))
      artifact(tasks.named("dokkaJavadocJar"))
      defaultConfigs(project = project, publicationName = "gpr")
      withBuildIdentifier()
    }

    register<MavenPublication>(name = "pluginMaven") {
      artifact(tasks.named("dokkaHtmlJar"))
      defaultConfigs(project = project, publicationName = "pluginMaven")
      withBuildIdentifier()
    }
  }
}



val test = testing.suites.getByName<JvmTestSuite>("test") {
  useKotlinTest(libs.versions.kotlin.get())
}

val functionalTest = testing.suites.register<JvmTestSuite>("functionalTest") {
  useKotlinTest(libs.versions.kotlin.get())

  dependencies {
    implementation(project())
  }

  targets {
    all {
      testTask.configure { shouldRunAfter(test) }
    }
  }
}

tasks {
  named<Task>("check") {
    dependsOn(functionalTest)
  }

  test {
    failOnNoDiscoveredTests = false
  }

  named<Test>("functionalTest") {
    failOnNoDiscoveredTests = false
  }

  withType<GenerateModuleMetadata> {
    dependsOn(dokkaJavadocJar)
  }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

dependencies {
  compileOnly(gradleKotlinDsl())
  compileOnly(libs.kotlin)
  dokkaHtmlPlugin(libs.dokka.plugin.versioning)
}
