val kotlin_version: String by project
val logback_version: String by project
val ktor_version: String by project
val mongo_version: String by project

plugins {
    application
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "2.3.13"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

group = "com.sindesoft"
version = "0.0.1"


application {
    mainClass.set("com.sindesoft.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks {
    create("stage").dependsOn("installDist")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-websockets-jvm:2.3.13")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    //MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:$mongo_version")
    implementation("org.mongodb:bson-kotlinx:$mongo_version")

    //WebSockets
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
}
