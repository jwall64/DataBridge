import buildlogic.Utils

plugins {
    id("build.fabric")
    id("build.publish")
}

Utils.setupResources(project, rootProject, "fabric.mod.json")

dependencies {

    minecraft("com.mojang:minecraft:${project.properties["minecraft-version"]}")
    implementation("net.fabricmc:fabric-loader:${project.properties["fabric-loader-version"]}")

    // Fabric API
    val apiModules = listOf(
        "fabric-api-base",
        "fabric-resource-loader-v0"
    )
    for(mod in apiModules) {
        api(include(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))!!)
    }

    include(api("me.lucko:fabric-permissions-api:0.7.0") {
        isTransitive = false
    })

    // Gametest API modules
    val testApiModules = listOf(
        "fabric-gametest-api-v1",
        "fabric-registry-sync-v0"
    )
    for(mod in testApiModules) {
        gametestImplementation(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))
    }
}

