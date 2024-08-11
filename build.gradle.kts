import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import net.nemerosa.versioning.VersioningExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.aot.ProcessAot
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {

    id("idea")
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.google.cloud.tools.jib") version "3.4.3"
    id("net.nemerosa.versioning") version "3.1.0"
    id("org.graalvm.buildtools.native") version "0.10.2"

    kotlin("jvm") version "2.0.10"
    kotlin("plugin.spring") version "2.0.10"

}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation("io.github.openfeign:feign-core:13.1")
    implementation("io.github.openfeign:feign-jackson:13.1")
    implementation("io.github.openfeign:feign-httpclient:13.1")

    implementation("org.slf4j:jcl-over-slf4j")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

configure<SpringBootExtension> {
    buildInfo()
}

configure<IdeaModel> {
    module {
        inheritOutputDirs = true
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
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

configure<VersioningExtension> {
    /**
     * Add GitHub CI branch name environment variable
     */
    branchEnv = listOf("GITHUB_REF_NAME")
}

extra {
    val build = getBuild()
    val versioning: VersioningExtension = extensions.getByName<VersioningExtension>("versioning")
    val branch = versioning.info.branch
    val shortCommit = versioning.info.commit.take(8)

    project.extra["build.date-time"] = build.buildDateAndTime
    project.extra["build.date"] = build.formattedBuildDate()
    project.extra["build.time"] = build.formattedBuildTime()
    project.extra["build.revision"] = versioning.info.commit
    project.extra["build.revision.abbreviated"] = shortCommit
    project.extra["build.branch"] = branch
    project.extra["build.user"] = build.userName()

    val containerImageName = "schaka/${project.name}"
    val containerImageTags = mutableSetOf(shortCommit, branch)
    if (branch.startsWith("v1")) {
        containerImageTags.add("stable")
    }

    project.extra["docker.image.name"] = containerImageName
    project.extra["docker.image.version"] = branch
    project.extra["docker.image.source"] = build.projectSourceRoot()
    project.extra["docker.image.tags"] = containerImageTags

    val nativeBaseTag = "native"
    val nativeImageName = "ghcr.io/${containerImageName}:$nativeBaseTag"
    val nativeImageTags = mutableListOf("$nativeImageName-$branch")
    if (branch.startsWith("v1")) {
        nativeImageTags.add("$nativeBaseTag-stable")
    }

    project.extra["native.image.name"] = nativeImageName
    project.extra["native.image.tags"] = nativeImageTags

}

tasks.withType<BootRun> {
    jvmArgs(
        arrayOf(
            "-Dspring.config.additional-location=optional:file:/config/application.yaml"
        )
    )
}

tasks.withType<ProcessAot> {
    args("-Dspring.config.additional-location=optional:file:/config/application.yaml")
}

tasks.withType<BootBuildImage> {

    docker.publishRegistry.url = "ghcr.io"
    docker.publishRegistry.username = System.getenv("USERNAME") ?: "INVALID_USER"
    docker.publishRegistry.password = System.getenv("GITHUB_TOKEN") ?: "INVALID_PASSWORD"

    imageName = project.extra["native.image.name"] as String
    version = project.extra["docker.image.version"] as String
    tags = project.extra["native.image.tags"] as List<String>
    createdDate = "now"

}

jib {
    to {
        image = "ghcr.io/${project.extra["docker.image.name"]}"
        tags = project.extra["docker.image.tags"] as Set<String>

        auth {
            username = System.getenv("USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    from {
        image = "eclipse-temurin:21-jre-jammy"
        auth {
            username = System.getenv("DOCKERHUB_USER")
            password = System.getenv("DOCKERHUB_PASSWORD")
        }
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    container {
        jvmFlags = listOf("-Dspring.config.additional-location=optional:file:/config/application.yaml", "-Xms256m")
        mainClass = "com.github.schaka.janitorr.JanitorrApplicationKt"
        ports = listOf("8978")
        format = ImageFormat.Docker
        volumes = listOf("/config")

        labels.set(
                mapOf(
                        "org.opencontainers.image.created" to "${project.extra["build.date"]}T${project.extra["build.time"]}",
                        "org.opencontainers.image.revision" to project.extra["build.revision"] as String,
                        "org.opencontainers.image.version" to project.version as String,
                        "org.opencontainers.image.title" to project.name,
                        "org.opencontainers.image.authors" to "Schaka <schaka@github.com>",
                        "org.opencontainers.image.source" to project.extra["docker.image.source"] as String,
                        "org.opencontainers.image.description" to project.description,
                )
        )


        // Exclude all "developmentOnly" dependencies, e.g. Spring devtools.
        configurationName.set("productionRuntimeClasspath")
    }
}