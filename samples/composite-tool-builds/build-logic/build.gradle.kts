plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

gradlePlugin {
  plugins {
    register("sampleConvention") {
      id = "sample.conventions"
      implementationClass = "sample.SampleConventionPlugin"
    }
  }
}
