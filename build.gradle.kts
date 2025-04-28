plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.slimjar)
    `maven-publish`
}

group = "com.volmit"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.crazydev22.de/public")
}

dependencies {
    implementation(libs.slimjar)

    // Kotlin standard library
    slim(libs.kotlin.stdlib)

    // Kotlin coroutines dependencies
    slim(libs.kotlin.coroutines)

    // Kotlin scripting dependencies
    slim(libs.kotlin.scripting.common)
    slim(libs.kotlin.scripting.jvm)
    slim(libs.kotlin.scripting.jvm.host)
    slim(libs.kotlin.scripting.dependencies.maven)

    compileOnly(fileTree("libs"))
    compileOnly(libs.caffeine)
    compileOnly(libs.spigot.api)
}

kotlin {
    jvmToolchain(21)
}

slimJar {
    globalRepositories = setOf(ArtifactRepositoryContainer.MAVEN_CENTRAL_URL)

    relocate("org.apache.maven", "com.volmit.iris.libs.maven")
    relocate("org.codehaus.plexus", "com.volmit.iris.libs.plexus")
    relocate("org.eclipse.sisu", "com.volmit.iris.libs.sisu")
}

tasks.shadowJar { dependsOn("slimJar") }

publishing.publications.create<MavenPublication>("maven") {
    from(components["shadow"])
    artifactId = project.name
}

afterEvaluate {
    tasks.named("generatePomFileForMavenPublication") {
        mustRunAfter("slimJar")
    }
}