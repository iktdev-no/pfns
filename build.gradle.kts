plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "1.5.31"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.5.5"
}

group = "no.iktdev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion = "0.61.0"
dependencies {
    implementation(kotlin("script-runtime"))

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation ("mysql:mysql-connector-java:8.0.29")

    implementation ("com.auth0:java-jwt:4.0.0")
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

    implementation("com.google.firebase:firebase-admin:9.2.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}