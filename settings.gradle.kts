rootProject.name = "janitorr"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/snapshot/")
        maven("https://repo.spring.io/milestone/")
        maven("https://repo.spring.io/plugins-release/")
        maven("https://repo.spring.io/plugins-snapshot/")
        maven("https://repo.spring.io/plugins-milestone/")
        maven("https://repo.spring.io/libs-release/")
        maven("https://repo.spring.io/libs-milestone/")
        maven("https://repo.spring.io/libs-snapshot/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}