package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

import org.gradle.api.file.DirectoryProperty
import java.io.File
import java.io.Serializable
import java.nio.file.Path

/**
 * Encapsulates the intent to declare and register a Gradle version catalog.
 *
 * Holds the catalog [name], optional [from] notation, and the [projectRoot] [DirectoryProperty].
 * Target file resolution is deferred to the [targetFile] property getter (or [resolve]), avoiding
 * premature evaluation of [projectRoot] during extension DSL configuration.
 *
 * @property name The name of the version catalog accessor (e.g. `"libs"`, `"tools"`).
 * @property from Optional file notation, path string, or object supplied during registration.
 * @property projectRoot [DirectoryProperty] pointing to the workspace root directory.
 */
internal class VersionCatalogIntention(
  val name: String,
  val from: Any? = null,
  val projectRoot: DirectoryProperty,
) : Serializable {

  /**
   * Lazily resolves the target catalog [File] on disk relative to [projectRoot].
   */
  val targetFile: File
    get() = resolve()

  /**
   * Resolves the target catalog [File] on disk relative to the current value of [projectRoot].
   *
   * @return The resolved [File] on disk.
   */
  fun resolve(): File {
    val rootDir = if (projectRoot.isPresent) {
      projectRoot.get().asFile
    } else {
      File(".")
    }
    return from.toResolvedFile(rootDir, name)
  }

  /**
   * Produces a human-readable diagnostic report describing the catalog intention.
   *
   * Format:
   * ```
   * Catalog Intention
   *  - name: ...
   *  - from: ...
   *  - projectRoot: ...
   *  - resolved: ...
   * ```
   *
   * @return Formatted multi-line diagnostic string.
   */
  fun report(): String {
    val fromDisplay = from?.let { "'$it'" } ?: "default location (gradle/$name.versions.toml)"
    val rootDisplay = if (projectRoot.isPresent) "'${projectRoot.get().asFile.path}'" else "<unspecified>"
    val resolvedDisplay = "'${resolve().path}'"
    return """
      Catalog Intention
       - name: $name
       - from: $fromDisplay
       - projectRoot: $rootDisplay
       - resolved: $resolvedDisplay
    """.trimIndent()
  }

  /**
   * Generates a formatted comparison message contrasting this intention with a [subsequent] replacement intention.
   *
   * Intended for consumption by the Gradle [org.gradle.api.problems.Problems] API diagnostic details.
   *
   * @param subsequent The new [VersionCatalogIntention] overwriting this intention.
   * @return Formatted multi-line string detailing the previous vs replacement state.
   */
  infix fun replacedBy(subsequent: VersionCatalogIntention): String {
    val previousFromDisplay = this.from?.let { "'$it'" } ?: "default location (gradle/${this.name}.versions.toml)"
    val newFromDisplay = subsequent.from?.let { "'$it'" } ?: "default location (gradle/${subsequent.name}.versions.toml)"
    return """
      Version catalog '${this.name}' was previously configured:
        - Input notation: $previousFromDisplay
        - Target file:    '${this.resolve().path}'
      It is now being replaced by:
        - Input notation: $newFromDisplay
        - Target file:    '${subsequent.resolve().path}'
      Only the subsequent instance will be registered.
    """.trimIndent()
  }

  private companion object {
    private const val serialVersionUID: Long = 1L

    private fun Any?.toResolvedFile(projectRoot: File, catalogName: String): File {
      if (this == null) {
        return File(projectRoot, "gradle/$catalogName.versions.toml")
      }
      val candidate = when (this) {
        is File -> this
        is Path -> this.toFile()
        is CharSequence -> {
          val str = this.toString()
          if (str.isEmpty()) {
            return File(projectRoot, "gradle/$catalogName.versions.toml")
          } else {
            File(str)
          }
        }
        else -> File(this.toString())
      }
      return if (candidate.isAbsolute) {
        candidate
      } else {
        File(projectRoot, candidate.path)
      }
    }
  }
}

/**
 * Derives a valid Gradle version catalog accessor name from a file notation, path string, or arbitrary object.
 *
 * Normalizes the base name by removing `.versions.toml` or `.toml` extensions and converting
 * kebab-case or snake_case separators into valid `lowerCamelCase` conforming to Gradle's
 * `^[a-z][a-zA-Z0-9]+$` catalog naming rules.
 *
 * @receiver The object, file, or path string representing a version catalog source.
 * @return The normalized `lowerCamelCase` catalog accessor name.
 * @throws IllegalArgumentException If the derived name is empty or starts with a non-lowercase ASCII letter.
 */
internal val Any.derivedCatalogName: String
  get() {
    val rawName = when (this) {
      is File -> this.name
      is Path -> this.fileName?.toString() ?: this.toString()
      else -> {
        val str = this.toString()
        val lastSlash = maxOf(str.lastIndexOf('/'), str.lastIndexOf('\\'))
        if (lastSlash >= 0) {
          str.substring(lastSlash + 1)
        } else {
          str
        }
      }
    }

    val baseName = when {
      rawName.endsWith(".versions.toml") -> rawName.removeSuffix(".versions.toml")
      rawName.endsWith(".toml") -> rawName.removeSuffix(".toml")
      else -> rawName
    }

    // Split on kebab-case, snake_case, spaces, or dots
    val parts = baseName.split('-', '_', '.', ' ').filter { it.isNotEmpty() }
    require(parts.isNotEmpty()) {
      "Cannot derive version catalog name from blank filename in '$this'"
    }

    val camelCase = buildString {
      append(parts.first().replaceFirstChar { it.lowercase() })
      for (part in parts.drop(1)) {
        append(part.replaceFirstChar { it.uppercase() })
      }
    }

    require(camelCase.matches(Regex("^[a-z][a-zA-Z0-9]+$"))) {
      "Derived version catalog name '$camelCase' from '$this' is invalid. Gradle catalog names must match regex ^[a-z][a-zA-Z0-9]+$"
    }

    return camelCase
  }
