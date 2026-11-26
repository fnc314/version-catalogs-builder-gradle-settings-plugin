# Implementation Plan: VersionCatalogIntention, Deriving Names & Default Catalog Alignment

**Status:** Completed  
**Date:** 2026-11-26  
**Target:** `version-catalogs-builder-gradle-settings-plugin`  
**Harness:** Antigravity  
**Model:** gemini-3.8-flash  
**Session:** `0118b492-57db-4b37-8fe5-8e5e5e554a4e`  

---

## 1. Executive Summary & Goals

Enhance version catalog registration in `version-catalogs-builder-gradle-settings-plugin` with four core capabilities:
1. **Encapsulated `internal` `VersionCatalogIntention` Helper:**
   - Define `VersionCatalogIntention` as `internal` (since it is an internal implementation model that does not leak into external consuming builds), but fully KDoc-documented:
     ```kotlin
     internal class VersionCatalogIntention(
       val name: String,
       val from: Any?,
       projectRoot: File,
     ) : Serializable {
       val targetFile: File = from.toResolvedFile(projectRoot, name)

       fun report(): String

       infix fun replacedBy(other: VersionCatalogIntention): String
     }
     ```
   - Eagerly computes `val targetFile: File` during construction using the extension's resolved `projectRoot` directory.
   - Clean, zero-argument `report(): String` describing `name`, `from`, and `targetFile.path`.
   - **NO `toString()` override**: `report()` is the deliberate, dedicated presentation method.
   - Member function `infix fun replacedBy(other: VersionCatalogIntention): String` defined **inside** `VersionCatalogIntention` for expressive multi-line problem diagnostic details:
     ```kotlin
     spec.details(previousIntention replacedBy newIntention)
     ```
2. **Third `catalog(from)` Overload with Name Derivation via Extension Property:**
   - Support `catalog(from: Any)` which delegates to `from.derivedCatalogName`.
   - Implement `internal val Any.derivedCatalogName: String get() { ... }` that strips `.versions.toml` (or `.toml`) and normalizes separators to **lowerCamelCase** to satisfy Gradle's `^[a-z][a-zA-Z0-9]+$` regex (`DefaultVersionCatalogBuilderContainer.VALID_EXTENSION_PATTERN`).
3. **Internal `catalogs` MapProperty & Clean Public Extension API:**
   - `VersionCatalogsBuilderGradleSettingsExtension` exposes public methods `catalog(name)`, `catalog(name, from)`, and `catalog(from)`.
   - The backing property is `internal val catalogs: MapProperty<String, VersionCatalogIntention>` so internal intention types are completely hidden from public consumers of the extension.
4. **Gradle Properties & Environment Configuration Hierarchy:**
   - Support configuring default catalog name and root override via `gradle.properties` as well as environment variables:
     - Default Catalog Name:
       1. `settings.providers.gradleProperty("dev.fnc314.version-catalogs-builder.default-catalog-name")`
       2. `System.getenv("VERSION_CATALOGS_BUILDER_DEFAULT_CATALOG_NAME")`
       3. `settings.dependencyResolutionManagement.defaultLibrariesExtensionName`
       4. Fallback to `"libs"`
     - Project Root Override:
       1. `settings.providers.gradleProperty("dev.fnc314.version-catalogs-builder.root")`
       2. `System.getenv("VERSION_CATALOGS_BUILDER_ROOT")`
       3. `System.getenv("WORKSPACE_ROOT")`
       4. Upwards filesystem traversal for `gradle-wrapper.properties` or `.git`
5. **Code Quality Standards:**
   - All `if/else` control flow strictly uses explicit curly braces `{ }`.
   - All `internal` declarations include strict, descriptive KDocs.

---

## 2. Technical Details & Architecture

### Intention Infix Reporting (Inside `VersionCatalogIntention`)
```kotlin
internal class VersionCatalogIntention(
  val name: String,
  val from: Any? = null,
  projectRoot: File,
) : Serializable {

  val targetFile: File = from.toResolvedFile(projectRoot, name)

  fun report(): String {
    val fromDisplay = if (from != null) {
      from.toString()
    } else {
      "default (gradle/$name.versions.toml)"
    }
    return buildString {
      append("Catalog Intention [name: '").append(name).append("']")
      append("\n  - From Notation: ").append(fromDisplay)
      append("\n  - Resolved Target: ").append(targetFile.path)
    }
  }

  infix fun replacedBy(other: VersionCatalogIntention): String {
    return """
      Version catalog '$name' declaration was overwritten by a subsequent declaration.
      Previous declaration:
      ${report().prependIndent("  ")}
      Replacement declaration:
      ${other.report().prependIndent("  ")}
      Only the second declaration will take effect.
    """.trimIndent()
  }

  private fun Any?.toResolvedFile(rootDir: File, catalogName: String): File {
    val relPath = when (this) {
      null -> "gradle/$catalogName.versions.toml"
      is File -> this.path
      is Path -> this.toString()
      else -> this.toString()
    }
    val candidate = File(relPath)
    return if (candidate.isAbsolute) {
      candidate
    } else {
      File(rootDir, relPath)
    }
  }
}
```

---

## 3. Proposed Changes

### [Plugin Implementation]

#### [NEW] `plugin/src/main/kotlin/dev/fnc314/gradle/plugins/settings/versioncatalogsbuildergradlesettingsplugin/VersionCatalogIntention.kt`
- Create `internal class VersionCatalogIntention` with member functions `report()`, member `infix fun replacedBy(...)`, encapsulated `toResolvedFile(...)`, and top-level `internal val Any.derivedCatalogName: String`.
- Complete file:
  ```kotlin
  package dev.fnc314.gradle.plugins.settings.versioncatalogsbuildergradlesettingsplugin

  import java.io.File
  import java.io.Serializable
  import java.nio.file.Path

  /**
   * Encapsulates a declared version catalog intention prior to Gradle registration.
   *
   * Resolves the target catalog file eagerly relative to [projectRoot].
   *
   * @property name The name of the version catalog accessor (e.g. "libs", "commonTools").
   * @property from The raw notation supplied to `catalog(name, from)` (String, File, Path, etc.), or `null` for default.
   * @param projectRoot The workspace root directory used to resolve relative paths.
   */
  internal class VersionCatalogIntention(
    val name: String,
    val from: Any? = null,
    projectRoot: File,
  ) : Serializable {

    /**
     * The resolved catalog [File] on disk.
     */
    val targetFile: File = from.toResolvedFile(projectRoot, name)

    /**
     * Formats an enriched diagnostic report of this catalog declaration for logging and problem reports.
     *
     * @return A structured, human-readable description of this intention.
     */
    fun report(): String {
      val fromDisplay = if (from != null) {
        from.toString()
      } else {
        "default (gradle/$name.versions.toml)"
      }
      return buildString {
        append("Catalog Intention [name: '").append(name).append("']")
        append("\n  - From Notation: ").append(fromDisplay)
        append("\n  - Resolved Target: ").append(targetFile.path)
      }
    }

    /**
     * Generates a multi-line diagnostic comparison explaining that this intention has been replaced by [other].
     *
     * @param other The replacement [VersionCatalogIntention].
     * @return Formatted multi-line diagnostic string detailing the replacement.
     */
    infix fun replacedBy(other: VersionCatalogIntention): String {
      return """
        Version catalog '$name' declaration was overwritten by a subsequent declaration.
        Previous declaration:
        ${report().prependIndent("  ")}
        Replacement declaration:
        ${other.report().prependIndent("  ")}
        Only the second declaration will take effect.
      """.trimIndent()
    }

    /**
     * Resolves a file notation to a target [File] relative to [rootDir].
     *
     * @param rootDir The workspace root directory.
     * @param catalogName Name of the catalog used for default file resolution.
     * @return Resolved [File] pointing to the version catalog TOML file.
     */
    private fun Any?.toResolvedFile(rootDir: File, catalogName: String): File {
      val relPath = when (this) {
        null -> "gradle/$catalogName.versions.toml"
        is File -> this.path
        is Path -> this.toString()
        else -> this.toString()
      }
      val candidate = File(relPath)
      return if (candidate.isAbsolute) {
        candidate
      } else {
        File(rootDir, relPath)
      }
    }
  }

  /**
   * Derives a valid Gradle catalog accessor name (lowerCamelCase) from a file or path notation.
   *
   * Strips `.versions.toml` or `.toml` extensions, removes path segments, and converts kebab-case
   * or snake_case separators to camelCase conforming to `[a-z][a-zA-Z0-9]+`.
   */
  internal val Any.derivedCatalogName: String
    get() {
      val rawName = when (this) {
        is File -> this.name
        is Path -> this.fileName.toString()
        else -> File(this.toString()).name
      }
      val stripped = rawName
        .removeSuffix(".versions.toml")
        .removeSuffix(".toml")

      val parts = stripped.split(Regex("[-_\\s]+")).filter { it.isNotEmpty() }
      if (parts.isEmpty()) {
        return "catalog"
      }

      val first = parts.first().lowercase()
      val remainder = parts.drop(1).joinToString("") { part ->
        part.replaceFirstChar { char ->
          if (char.isLowerCase()) {
            char.titlecase()
          } else {
            char.toString()
          }
        }
      }
      val candidate = first + remainder
      val sanitized = candidate.filter { it.isLetterOrDigit() }
      return if (sanitized.isNotEmpty() && sanitized.first().isLowerCase()) {
        sanitized
      } else {
        "c" + sanitized
      }
    }
  ```

#### [MODIFY] `plugin/src/main/kotlin/dev/fnc314/gradle/plugins/settings/versioncatalogsbuildergradlesettingsplugin/VersionCatalogsBuilderGradleSettingsExtension.kt`
- Keep `catalogs` internal so internal `VersionCatalogIntention` does not expose public API surface:
  ```kotlin
  internal val catalogs: MapProperty<String, VersionCatalogIntention> =
    objects.mapProperty(String::class.java, VersionCatalogIntention::class.java)
  ```
- Expose three public methods:
  ```kotlin
  /**
   * Registers a version catalog by [name], defaulting to `gradle/{name}.versions.toml` relative to [projectRoot].
   *
   * Example:
   * ```kotlin
   * versionCatalogsBuilder {
   *   catalog("libs")
   * }
   * ```
   *
   * @param name Name of the version catalog accessor (e.g. `"libs"`, `"tools"`).
   */
  public fun catalog(name: String) {
    recordCatalog(name, null)
  }

  /**
   * Registers a version catalog by [name] with an explicit [from] path or file reference.
   *
   * Accepts any argument supported by Gradle file notation (`String`, `File`, `Path`, etc.).
   * Relative paths are resolved relative to [projectRoot].
   *
   * Example:
   * ```kotlin
   * versionCatalogsBuilder {
   *   catalog("tools", "gradle/tools.versions.toml")
   * }
   * ```
   *
   * @param name Name of the version catalog accessor.
   * @param from File, relative path string, or notation pointing to the TOML catalog file.
   */
  public fun catalog(name: String, from: Any) {
    recordCatalog(name, from)
  }

  /**
   * Registers a version catalog by deriving its name from the specified [from] path or file notation.
   *
   * For example, `"gradle/common-tools.versions.toml"` derives catalog name `"commonTools"`.
   *
   * @param from File, relative path string, or notation pointing to the TOML catalog file.
   */
  public fun catalog(from: Any) {
    recordCatalog(from.derivedCatalogName, from)
  }
  ```
- In `recordCatalog`, clean call site with `replacedBy`:
  ```kotlin
  private fun recordCatalog(name: String, from: Any?) {
    val rootDir = projectRoot.get().asFile
    val previousIntention = catalogs.getting(name).orNull
    val newIntention = VersionCatalogIntention(name, from, rootDir)
    if (previousIntention != null) {
      val problemId = problems.groups.dependencies.declaration.problemId("Version catalog declaration overwritten")
      problems.reporter.report(problemId) { spec ->
        spec
          .contextualLabel("Version catalog '$name' declaration was overwritten")
          .details(previousIntention replacedBy newIntention)
          .solution("Remove duplicate catalog declarations or specify distinct catalog names")
      }
    }
    catalogs.put(name, newIntention)
  }
  ```

#### [MODIFY] `plugin/src/main/kotlin/dev/fnc314/gradle/plugins/settings/versioncatalogsbuildergradlesettingsplugin/VersionCatalogsBuilderGradleSettingsPlugin.kt`
- Default catalog name resolution:
  ```kotlin
  private fun resolveDefaultCatalogName(settings: Settings): String {
    // 1. gradle.properties override
    settings.providers.gradleProperty("dev.fnc314.version-catalogs-builder.default-catalog-name").orNull
      ?.takeIf { it.isNotBlank() }
      ?.let { return it }

    // 2. Environment variable override
    System.getenv("VERSION_CATALOGS_BUILDER_DEFAULT_CATALOG_NAME")
      ?.takeIf { it.isNotBlank() }
      ?.let { return it }

    // 3. Gradle standard DependencyResolutionManagement setting
    return settings.dependencyResolutionManagement.defaultLibrariesExtensionName.getOrElse("libs")
  }
  ```
- Project root resolution:
  ```kotlin
  private fun resolveRoot(settings: Settings): File {
    // 1. gradle.properties override
    settings.providers.gradleProperty("dev.fnc314.version-catalogs-builder.root").orNull
      ?.let { File(it) }?.takeIf { it.exists() }?.let { return it }

    // 2. Specific plugin root override
    System.getenv("VERSION_CATALOGS_BUILDER_ROOT")?.let { File(it) }?.takeIf { it.exists() }?.let { return it }

    // 3. Continuous integration / monorepo workspace override
    System.getenv("WORKSPACE_ROOT")?.let { File(it) }?.takeIf { it.exists() }?.let { return it }

    // 4. Upwards filesystem traversal for gradle/wrapper/gradle-wrapper.properties or .git
    var current: File? = settings.settingsDir
    while (current != null) {
      if (File(current, "gradle/wrapper/gradle-wrapper.properties").exists() || File(current, ".git").exists()) {
        return current
      }
      val parent = current.parentFile ?: break
      current = parent
    }

    // 5. Fallback
    return current ?: settings.settingsDir
  }
  ```
- In `settingsEvaluated`:
  ```kotlin
  val catalogMap = extension.catalogs.get()
  for ((name, intention) in catalogMap) {
    val catalogFile = intention.targetFile
    if (!catalogFile.exists()) {
      continue
    }

    val isDefaultLibs = name == defaultCatalogName && defaultLibsFile.exists() && catalogFile.canonicalFile == defaultLibsFile.canonicalFile
    if (isDefaultLibs) {
      continue
    }

    if (name == defaultCatalogName && defaultLibsFile.exists()) {
      val problemId = problems.groups.dependencies.declaration.problemId("Conflicting version catalog file")
      throw problems.reporter.throwing(
        IllegalStateException(
          "Version catalog '$name' is already registered by Gradle from '${defaultLibsFile.path}', " +
            "which conflicts with requested catalog target '${catalogFile.path}'. " +
            "Remove the local gradle/libs.versions.toml or use a distinct catalog name."
        ),
        problemId,
      ) { spec ->
        spec
          .contextualLabel("Version catalog '$name' conflicts with existing default catalog")
          .details("Attempted to register:\n${intention.report()}\nwhich conflicts with default '${defaultLibsFile.path}'.")
          .solution("Remove the local gradle/libs.versions.toml or use a distinct catalog name")
      }
    }

    val existing = settings.dependencyResolutionManagement.versionCatalogs.findByName(name)
    if (existing != null) {
      val problemId = problems.groups.dependencies.declaration.problemId("Duplicate version catalog declaration")
      throw problems.reporter.throwing(
        IllegalStateException("Version catalog '$name' is already registered, conflicting with '${catalogFile.path}'."),
        problemId,
      ) { spec ->
        spec
          .contextualLabel("Version catalog '$name' is already registered")
          .details("Attempted to register:\n${intention.report()}\nwhich conflicts with existing catalog.")
          .solution("Use a distinct catalog name for each version catalog")
      }
    }

    val fileCollection = settings.layout.settingsDirectory.files(catalogFile)
    settings.dependencyResolutionManagement.versionCatalogs.create(name) { catalog ->
      catalog.from(fileCollection)
    }
  }
  ```

---

## 4. Verification Plan & Testing Checklist

- [ ] `./gradlew check` passes with all tests.
- [ ] Add functional test for single-argument `catalog("custom/network-tools.versions.toml")` confirming derived name `networkTools`.
- [ ] Add test verifying `dev.fnc314.version-catalogs-builder.default-catalog-name` and `VERSION_CATALOGS_BUILDER_DEFAULT_CATALOG_NAME`.
- [ ] Add test for `replacedBy` verifying output formatting.
- [ ] Run `:dokkaCapture` and `:dokkaVersion` to update documentation.
- [ ] Ensure 2-space indentation, strict KDocs, and braces on all `if/else` statements.

---

## 5. Bookkeeping
- [x] Plan persisted at `.agents/plans/2026-11-26/034320_version-catalog-intention-wrapper.md`
- [x] Registry `README.md` row updated
