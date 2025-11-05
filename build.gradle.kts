plugins {
    id("fabric-loom") version "1.5-SNAPSHOT"
    kotlin("jvm") version "1.9.23"
}

version = "1.0.0"
group = "com.example.flashblade"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("com.mojang:minecraft:1.20.1")
    implementation("net.fabricmc:yarn:1.20.1+build.10:v2")
    implementation("net.fabricmc:fabric-loader:0.15.6")
    implementation("net.fabricmc.fabric-api:fabric-api:0.92.0+1.20.1")
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(17)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

// Pin Gradle wrapper to latest stable
tasks.wrapper {
    gradleVersion = "8.10.2"
    distributionType = Wrapper.DistributionType.BIN
}
