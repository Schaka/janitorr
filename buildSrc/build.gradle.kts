import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22

group = "com.github.schaka.janitorr"

plugins {
    kotlin("jvm") version "2.0.20"
    `kotlin-dsl` version "5.1.1"
    `embedded-kotlin` version "5.1.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_22.toString()
    targetCompatibility = JavaVersion.VERSION_22.toString()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JVM_22
    }
}