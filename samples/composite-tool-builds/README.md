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
