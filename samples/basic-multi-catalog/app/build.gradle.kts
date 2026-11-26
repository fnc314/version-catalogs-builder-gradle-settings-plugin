plugins {
  `java-library`
}

dependencies {
  // Consuming from libs catalog
  implementation(libs.coroutines.core)
  // Consuming from tools catalog
  compileOnly(tools.detekt.cli)
}
