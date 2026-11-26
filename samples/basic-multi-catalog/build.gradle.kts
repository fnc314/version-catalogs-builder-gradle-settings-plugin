plugins {
  base
}

tasks.register("buildAll") {
  dependsOn(subprojects.map { it.tasks.named("help") })
  doLast {
    println("All subprojects evaluated successfully with multiple version catalogs!")
  }
}
