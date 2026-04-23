rootProject.name = "janitorr"

pluginManagement {
    val foojayResolverVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val versioningPluginVersion: String by settings
    val kotlinVersion: String by settings

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolverVersion
        id("org.springframework.boot") version springBootVersion
        id("org.springframework.boot.aot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("net.nemerosa.versioning") version versioningPluginVersion
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        maven("https://repo.spring.io/plugins-milestone")
        maven("https://repo.spring.io/plugins-snapshot")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}