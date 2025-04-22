plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.shadow)
    `maven-publish`
}

group = "com.volmit"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Kotlin standard library
    implementation(libs.kotlin.stdlib)

    // Kotlin coroutines dependencies
    implementation(libs.kotlin.coroutines)

    // Kotlin scripting dependencies
    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)
    implementation(libs.kotlin.scripting.dependencies.maven)

    compileOnly(fileTree("libs"))
    compileOnly(libs.caffeine)
    compileOnly(libs.spigot.api)
}

kotlin {
    jvmToolchain(21)
}

publishing.publications.create<MavenPublication>("maven") {
    from(components["shadow"])
    artifactId = project.name
}