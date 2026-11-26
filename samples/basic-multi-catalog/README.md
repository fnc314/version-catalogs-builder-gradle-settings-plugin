# Sample: Basic Multi-Catalog

This sample demonstrates how to configure and resolve multiple version catalogs using `version-catalogs-builder-gradle-settings-plugin`:
- **Convention syntax**: `catalog("libs")` resolves automatically to `gradle/libs.versions.toml`.
- **Derived name syntax**: `catalog("gradle/network-tools.versions.toml")` derives catalog accessor `networkTools`.
- **Explicit path syntax**: `catalog("tools", "gradle/tools.versions.toml")` resolves to a customized catalog file.

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
