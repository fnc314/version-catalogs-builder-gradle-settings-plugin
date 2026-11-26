package sample

import org.gradle.api.Plugin
import org.gradle.api.Project

class SampleConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.tasks.register("greeting") {
      doLast {
        println("Hello from build-logic convention plugin in " + target.path + "!")
      }
    }
  }
}
