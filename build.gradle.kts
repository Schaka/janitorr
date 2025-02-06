import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import net.nemerosa.versioning.VersioningExtension
import org.gradle.kotlin.dsl.invoke
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.aot.ProcessAot
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {

    id("idea")
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.cloud.tools.jib") version "3.4.4"
    id("net.nemerosa.versioning") version "3.1.0"
    id("org.graalvm.buildtools.native") version "0.10.5"

    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"

}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

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

// Required until GraalVM/Paketo builders receive a fix
sourceSets {
    main {
        java {
            srcDir("src/main")
            srcDir("src/java.base")
        }
        kotlin {
            srcDir("src/kotlin")
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_22.toString()
    targetCompatibility = JavaVersion.VERSION_22.toString()

    finalizedBy("copyPatches")
}

/*
 * Hack required until
 * - https://github.com/paketo-buildpacks/native-image/issues/344
 * - https://github.com/oracle/graal/issues/9879
 * are fixed.
 *
 * We're copying over patches to the JDK and forcing them into the native image at build time.
 */
tasks.register<Copy>("copyPatches") {
    dependsOn("build")
    mustRunAfter("compileJava")

    from(layout.buildDirectory.dir("classes/java/main"))
    include("**/*.*")
    into(layout.buildDirectory.dir("resources/main/java.base"))
}

tasks.register("buildPatchedNativeImage")

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JVM_22
        javaParameters = true
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
    val branch = versioning.info.branch.replace("/", "-")
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
    if (branch.startsWith("v")) {
        containerImageTags.add("stable")
    }

    project.extra["docker.image.name"] = containerImageName
    project.extra["docker.image.version"] = branch
    project.extra["docker.image.source"] = build.projectSourceRoot()
    project.extra["docker.image.tags"] = containerImageTags

    val platform = System.getenv("TARGET_PLATFORM") ?: "amd64"
    val nativeBaseTag = "native-$platform"
    val nativeImageName = "ghcr.io/${containerImageName}:$nativeBaseTag"
    val nativeImageTags = listOf("$nativeImageName-$branch")

    project.extra["native.image.name"] = nativeImageName
    project.extra["native.image.tags"] = nativeImageTags

}

tasks.withType<BootRun> {
    jvmArgs(
        arrayOf(
            "-Dspring.config.additional-location=optional:file:/config/application.yaml,optional:file:/workspace/application.yaml,optional:file:/workspace/application.yml",
            "-Dsun.jnu.encoding=UTF-8",
            "-Dfile.encoding=UTF-8"
        )
    )
}

tasks.withType<ProcessAot> {
    args(
        "-Dspring.config.additional-location=optional:file:/config/application.yaml,optional:file:/workspace/application.yaml,optional:file:/workspace/application.yml",
        "-Dsun.jnu.encoding=UTF-8",
        "-Dfile.encoding=UTF-8"
    )
}

tasks.withType<BootBuildImage> {

    docker.publishRegistry.url = "ghcr.io"
    docker.publishRegistry.username = System.getenv("USERNAME") ?: "INVALID_USER"
    docker.publishRegistry.password = System.getenv("GITHUB_TOKEN") ?: "INVALID_PASSWORD"

    builder = "paketobuildpacks/builder-jammy-buildpackless-tiny"
    buildpacks = listOf(
        "paketobuildpacks/environment-variables",
        "paketobuildpacks/java-native-image",
        "paketobuildpacks/health-checker"
    )
    imageName = project.extra["native.image.name"] as String
    version = project.extra["docker.image.version"] as String
    tags = project.extra["native.image.tags"] as List<String>
    createdDate = "now"

    // It would also be possible to set this in the graalVmNative block, but we don't want to overwrite Spring's settings
    environment = mapOf(
        "BP_NATIVE_IMAGE" to "true",
        "BPL_SPRING_AOT_ENABLED" to "true",
        "BP_HEALTH_CHECKER_ENABLED" to "true",
        "BP_JVM_CDS_ENABLED" to "true",
        "BP_JVM_VERSION" to "23",
        "BPE_LANG" to "en_US.UTF-8",
        "BPE_LANGUAGE" to "LANGUAGE=en_US:en",
        "BPE_LC_ALL" to "en_US.UTF-8",
        "BP_NATIVE_IMAGE_BUILD_ARGUMENTS" to "-march=compatibility -H:+AddAllCharsets -J--patch-module=java.base=/workspace/BOOT-INF/classes/java.base"
    )
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
        image = "eclipse-temurin:23-jre-noble"
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
        jvmFlags = listOf(
            "-Dspring.config.additional-location=optional:file:/config/application.yaml,optional:file:/workspace/application.yaml,optional:file:/workspace/application.yml",
            "-Dsun.jnu.encoding=UTF-8",
            "-Dfile.encoding=UTF-8",
            "-Xms256m",
        )
        mainClass = "com.github.schaka.janitorr.JanitorrApplicationKt"
        ports = listOf("8978")
        format = ImageFormat.Docker // OCI not yet supported
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