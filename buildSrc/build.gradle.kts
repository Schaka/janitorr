import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.schaka.janitorr"

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
}