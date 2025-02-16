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

    minecraft("com.mojang:minecraft:1.21.4")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.10")

    // Fabric API
    val apiModules = listOf(
        "fabric-api-base",
        "fabric-resource-loader-v0"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "0.116.1+1.21.4"))!!)
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
