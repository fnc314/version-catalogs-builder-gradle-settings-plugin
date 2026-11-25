@file:Suppress("UnstableApiUsage")
package dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.projectplugins

import org.gradle.plugin.compatibility.compatibility

plugins {
  // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
  id("com.gradle.plugin-publish")
  `java-gradle-plugin`
  `maven-publish`

  // Apply the `signing` plugin and configure appropriately
  // https://docs.gradle.org/current/userguide/signing_plugin.html
  signing
}

signing {
  val signingKey = providers.environmentVariable("SIGNING_KEY")
    .orElse(providers.gradleProperty("signingKey"))
  val signingPassword = providers.environmentVariable("SIGNING_PASSWORD")
    .orElse(providers.gradleProperty("signingPassword"))

  if (signingKey.isPresent) {
    useInMemoryPgpKeys(signingKey.get(), signingPassword.orNull)
  }

  isRequired = signingKey.isPresent
}

gradlePlugin {
  website = "https://www.fnc314.dev/${project.name}"
  vcsUrl = "https://github.com/fnc314/${project.name}"
  isAutomatedPublishing = true
  // Define the plugin
  plugins.create("settingsSubsystemGradleSettingsPlugin") {
    id = "dev.fnc314.gradle.plugins.settings.${project.name}"
    implementationClass = "dev.fnc314.gradle.plugins.settings.settingssubsystemgradlesettingsplugin.SettingsSubsystemGradleSettingsPlugin"
    tags = listOf("gradle.settings", "settings.plugin", "subsystem", "catalogs")
    description = "Settings convention plugin orchestrating workspace root discovery, catalog loading, and tool build inclusion"
    displayName = "settings-subsystem-gradle-settings-plugin"
    compatibility {
      features {
        configurationCache = true
        isolatedProjects = true
      }
    }
  }
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/fnc314/${project.name}")
      credentials {
        val actor = providers.environmentVariable("GITHUB_ACTOR")
          .orElse(providers.environmentVariable("USER"))
          .orElse("fnc314")
        val token = providers.environmentVariable("GITHUB_TOKEN").orElse("")

        username = actor.get()
        password = token.get()
      }
    }
  }
}
