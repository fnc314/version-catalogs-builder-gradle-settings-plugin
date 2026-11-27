# Sample: Multi-Project Shared Catalog

This sample demonstrates how one `libs` version catalog can be shared by multiple projects in the same Gradle build. Both `:app` and `:core` consume the `libs.coroutines.core` alias.

Maven Central is configured by the consumer build, not by the version-catalogs-builder plugin.

## Project Structure

```
.
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── app/
│   └── build.gradle.kts
└── core/
    └── build.gradle.kts
```

## Verifying the Build

Run the following commands inside this directory:
```bash
../../gradlew verifySharedCatalog
../../gradlew :app:dependencies
```
