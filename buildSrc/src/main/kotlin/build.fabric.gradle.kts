import buildlogic.Utils

plugins {
    id("build.common")
    id("net.fabricmc.fabric-loom")
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "${rootProject.name}-tests"
        enableGameTests = true
        enableClientGameTests = true
        eula = true
        clearRunDirectory = true
        username = "Player0"
    }
}

val workingDir = project.projectDir.toPath()
val tmpDir = System.getProperty("java.io.tmpdir")

loom {
    runs {
        getByName("client") {
            runDir = "run/client"
            ideConfigGenerated(false)
            client()
        }
        getByName("server") {
            runDir = "run/server"
            ideConfigGenerated(false)
            server()
        }
        getByName("gameTest") {
            val randomString = List(16) { ('a'..'z').random() }.joinToString("")
            val tmpRunDir = file("${tmpDir}/gt_${randomString}").toPath()
            runDir = workingDir.relativize(tmpRunDir).toString()
        }
        getByName("clientGameTest") {
            val randomString = List(16) { ('a'..'z').random() }.joinToString("")
            val tmpRunDir = file("${tmpDir}/gtc_${randomString}").toPath()
            runDir = workingDir.relativize(tmpRunDir).toString()
        }
        register("testClient") {
            name = "Test Client"
            runDir = "run/testClient"
            ideConfigGenerated(false)
            client()
            source(sourceSets.getByName("gametest"))
        }
        register("testServer") {
            name = "Test Server"
            runDir = "run/testServer"
            ideConfigGenerated(false)
            server()
            source(sourceSets.getByName("gametest"))
        }
    }
    mixin {
        defaultRefmapName = "${rootProject.name}.refmap.json"
    }
}

val archiveName = Utils.getArchiveName(project)
Utils.setupResources(project, rootProject, "fabric.mod.json")
