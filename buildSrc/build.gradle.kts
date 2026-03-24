plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.2.2")
    implementation("net.fabricmc:fabric-loom:1.15-SNAPSHOT")
    implementation("org.gradle.toolchains.foojay-resolver-convention:org.gradle.toolchains.foojay-resolver-convention.gradle.plugin:1.0.0")
}

//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
//}
