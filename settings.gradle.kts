pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // https://stonecutter.kikugie.dev
    id("dev.kikugie.stonecutter") version "0.9.7"
    // Applies the correct Loom variant across the 26.1+ (unobfuscated) boundary.
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    // Helps Gradle provision the right JDK toolchains (21 / 25).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("1.21.1", "26.2")
        vcsVersion = "26.2"
    }
}

rootProject.name = "Weathering Chamber"
