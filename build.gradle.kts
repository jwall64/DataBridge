import buildlogic.Utils

plugins {
    id("build.fabric")
    id("build.publish")
}

Utils.setupResources(project, rootProject, "fabric.mod.json")

dependencies {

    minecraft("com.mojang:minecraft:${project.properties["minecraft-version"]}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric-loader-version"]}")

    // Fabric API
    val apiModules = listOf(
        "fabric-api-base",
        "fabric-resource-loader-v0"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))!!)
    }

    include(modApi("me.lucko:fabric-permissions-api:0.6.1") {
        isTransitive = false
    })

    // Gametest API modules
    val testApiModules = listOf(
        "fabric-gametest-api-v1",
        "fabric-registry-sync-v0"
    )
    for(mod in testApiModules) {
        modGametestImplementation(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))
    }
}

