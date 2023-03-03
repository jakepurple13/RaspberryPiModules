plugins {
    kotlin("jvm") version "1.8.0"
    id("maven-publish")
}

group = "com.programmersbox.serverlibraries"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jmdns:jmdns:3.5.8")
    implementation("org.slf4j:slf4j-nop:2.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}