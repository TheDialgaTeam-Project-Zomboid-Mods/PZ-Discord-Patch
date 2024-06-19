plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val rootDir = "E:/SteamLibrary/steamapps/common/Project Zomboid Dedicated Server/"

java {
    group = "org.gatewayroleplay"
    version = "1.0.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(files("$rootDir/java/"))

    compileOnly(fileTree("$rootDir/java/") {
        include("*.jar")
        exclude(rootProject.name + "-*.jar")
    })

    implementation("org.javacord:javacord:3.8.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
}