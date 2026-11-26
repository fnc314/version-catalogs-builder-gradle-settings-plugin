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
