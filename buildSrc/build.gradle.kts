plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        url = uri("https://maven.jwall64.dev/releases/")
    }
    maven {
        url = uri("https://maven.jwall64.dev/plugins/")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.6.1")
    implementation("net.fabricmc:fabric-loom:1.19-SNAPSHOT")
    implementation("dev.jwall64:gradle-pack-uploader:0.4.0")
}

