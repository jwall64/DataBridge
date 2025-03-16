import buildlogic.Utils

plugins {
    id("build.fabric")
    id("build.publish")
}

Utils.setupResources(project, rootProject, "fabric.mod.json")

sourceSets {
    register("testmod") {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

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

    include(modApi("me.lucko:fabric-permissions-api:0.3.3") {
        isTransitive = false
    })
}

loom {
    mods {
        register(project.name + "-testmod") {
            sourceSet(sourceSets["testmod"])
        }
    }
    runs {
        register("testmodServer") {
            server()
            ideConfigGenerated(false)
            runDir = "run/testserver"
            name = "Testmod Server"
            source(sourceSets.getByName("testmod"))
        }
    }
}
