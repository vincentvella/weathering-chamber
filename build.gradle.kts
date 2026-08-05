plugins {
    // loom-back-compat applies the right fabric-loom variant per Minecraft version.
    id("dev.kikugie.loom-back-compat")
}

// Do NOT set group here (loom-back-compat manages it).
version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

// Minecraft 26.1+ requires Java 25; 1.20.5+ requires Java 21.
val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1"   -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else                                   -> JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    // Applies Mojang (official) mappings on obfuscated versions; a no-op on 26.1+.
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

tasks.processResources {
    // NB: inside this block the receiver is the task, whose own `property()` would
    // shadow the project's — so qualify with `project.` to read the Stonecutter
    // centralized (version-specific) property and the project version.
    val props = mapOf(
        "version" to project.version,
        "minecraft" to project.property("mod.mc_compat")
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}
