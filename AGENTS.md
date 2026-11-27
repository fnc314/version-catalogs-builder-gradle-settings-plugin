# Agent Guidelines: Version Catalogs Builder Gradle Settings Plugin

This repository provides `version-catalogs-builder-gradle-settings-plugin`, a Gradle Settings convention plugin dedicated strictly to building and registering Gradle version catalogs.

---

## 1. Core Mission & Scope Boundary (MANDATORY)

**The project builds version catalogs and builds them ONLY.**

Any contribution or agent task must respect these strict boundaries:
- **NO repository handler configuration:** The plugin must NOT configure `pluginManagement.repositories` or `dependencyResolutionManagement.repositories`.
- **NO feature preview flags:** The plugin must NOT enable feature previews such as `TYPESAFE_PROJECT_ACCESSORS`.
- **NO tool builds registration:** The plugin must NOT include composite tool builds (`includeBuild`).
- **Single Responsibility:** The extension exposes ONLY `projectRoot` (`DirectoryProperty`) and direct catalog declarations:
  ```kotlin
  versionCatalogsBuilder {
    projectRoot.set(...) // DirectoryProperty (defaults to auto-discovered workspace root)
    catalog(name: String) // defaults to $projectRoot/gradle/$name.versions.toml
    catalog(name: String, from: Any) // resolves file notation relative to projectRoot or Settings.file
  }
  ```
- **DSL Safety:** All extension and builder classes must be annotated with `@VersionCatalogsBuilderDsl` (`@DslMarker`).

---

## 2. Architecture Planning Paradigm (MANDATORY)

Every plan created in this repository must strictly adhere to the architecture planning paradigm documented in [`.agents/plans/README.md`](.agents/plans/README.md) and [`TEMPLATE.md`](.agents/plans/TEMPLATE.md):

1. **Target Directory**: Persist plans in `.agents/plans/YYYY-MM-DD/` using a 24-hour timestamp prefix: `HHMMSS_<plan-name>.md` (kebab-case).
2. **Template Frontmatter**: Every plan must open with mandatory metadata:
   - `Status`: `Draft / Proposed`, `In Progress`, `Completed`, `Superseded`, etc.
   - `Date`: `YYYY-MM-DD`
   - `Target`: Scope of the changes
   - `Harness`: Running harness name (e.g. Antigravity, Junie, Claude Code)
   - `Model`: Running model identifier
   - `Session`: Session/conversation ID
3. **Registry Synchronization**: Register the plan immediately in [`.agents/plans/README.md`](.agents/plans/README.md) table with matching status.
4. **No Plan Artifact Drifting**: Keep plans synchronized with actual implementation decisions.

---

## 3. Code Quality & Formatting Standards

- **Explicit API Mode**: The Kotlin compiler runs with `-Xexplicit-api=strict`. All public declarations (classes, functions, properties) must have explicit visibility modifiers and return types.
- **Comprehensive KDocs**: All `public` declarations must include descriptive KDoc comments detailing parameters, return types, and usage examples.
- **Two-Space Indentation**: All Kotlin source files and `.gradle.kts` build files adhere to 2-space indentation.
- **Gradle Isolated Projects Readiness**: Avoid referencing unstable or deprecated project-state APIs across boundaries; resolve files cleanly against `Settings` and `DirectoryProperty`.

---

## 4. Verification & Safety Guardrails

- **Automated Testing**: Before marking any task complete, run `./gradlew check`. All unit tests, Gradle plugin validation tasks, and functional tests must pass.
- **Never Auto-Publish**: Do NOT execute publishing or release tasks (such as `publish`, `publishToMavenLocal`, `publishPlugins`) without explicit user instruction. Prepare releases by updating versions, documentation, and tests, but do not push to public registries.
- **Release Version Synchronization**: Keep the project version in `gradle/libs.versions.toml`, `gradle.properties`, `build-logic/gradle.properties`, and `mise.toml` (`PROJECT_VERSION`) aligned with the plugin versions in all sample `settings.gradle.kts` files, the `README.md` badge and plugin examples, the `dokka.md` plugin examples, and the `version/X.Y.Z` Git tag. Generate `docs/index.html` from `README.md` with `mise run generate:docs`; never hand-edit the generated page. For release version changes or tag/push operations, strictly use `mise run release X.Y.Z`; do not update these values or create/push release tags manually. Run this command only when the user explicitly authorizes the release operation.
