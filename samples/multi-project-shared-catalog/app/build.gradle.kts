plugins {
  `java-library`
}

dependencies {
  implementation(project(":core"))
  implementation(libs.coroutines.core)
}
