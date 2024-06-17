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
    compileOnly(files("$rootDir/java/commons-compress-1.18.jar"))
    compileOnly(files("$rootDir/java/istack-commons-runtime.jar"))
    compileOnly(files("$rootDir/java/jassimp.jar"))
    compileOnly(files("$rootDir/java/javacord-2.0.17-shaded.jar"))
    compileOnly(files("$rootDir/java/javax.activation-api.jar"))
    compileOnly(files("$rootDir/java/jaxb-api.jar"))
    compileOnly(files("$rootDir/java/jaxb-runtime.jar"))
    compileOnly(files("$rootDir/java/lwjgl.jar"))
    compileOnly(files("$rootDir/java/lwjgl_util.jar"))
    compileOnly(files("$rootDir/java/lwjgl-glfw.jar"))
    compileOnly(files("$rootDir/java/lwjgl-jemalloc.jar"))
    compileOnly(files("$rootDir/java/lwjgl-opengl.jar"))
    compileOnly(files("$rootDir/java/serialize.jar"))
    compileOnly(files("$rootDir/java/sqlite-jdbc-3.27.2.1.jar"))
    compileOnly(files("$rootDir/java/trove-3.0.3.jar"))
    compileOnly(files("$rootDir/java/uncommons-maths-1.2.3.jar"))

    implementation("org.javacord:javacord:3.8.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
}