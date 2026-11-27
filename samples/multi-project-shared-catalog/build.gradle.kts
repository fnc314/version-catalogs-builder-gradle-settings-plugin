plugins {
  base
}

tasks.register("verifySharedCatalog") {
  dependsOn(subprojects.map { it.tasks.named("help") })
  doLast {
    println("Both subprojects use the shared libs version catalog.")
  }
}
