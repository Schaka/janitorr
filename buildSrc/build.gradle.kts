import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.schaka.janitorr"

plugins {
    kotlin("jvm") version "2.2.20"
    `kotlin-dsl` version "6.4.1"
    `embedded-kotlin` version "6.4.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_24.toString()
    targetCompatibility = JavaVersion.VERSION_24.toString()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_24
    }
}