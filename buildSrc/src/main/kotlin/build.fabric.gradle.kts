import buildlogic.Utils
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask

plugins {
    id("build.common")
    id("build.shadow")
    id("net.fabricmc.fabric-loom")
    id("com.gradleup.shadow")
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

val archiveName = Utils.getArchiveName(project, rootProject)

tasks.named<Jar>("jar") {
    archiveBaseName.set(archiveName)
    archiveClassifier.set("partial")
}

tasks.named<ShadowJar>("shadowJar") {
    enabled = true
    archiveBaseName.set(archiveName)
    archiveClassifier.set("partial")
}

