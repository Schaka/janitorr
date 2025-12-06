
import net.nemerosa.versioning.VersioningExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.aot.ProcessAot
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {

    id("idea")
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot.aot") version "3.5.6"
    id("net.nemerosa.versioning") version "3.1.0"
    id("org.graalvm.buildtools.native") version "0.11.0"

    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"

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
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("io.github.openfeign:feign-core:13.6")
    implementation("io.github.openfeign:feign-jackson:13.6")
    implementation("io.github.openfeign:feign-httpclient:13.5")

    implementation("org.slf4j:jcl-over-slf4j")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.5")
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
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_24.toString()
    targetCompatibility = JavaVersion.VERSION_24.toString()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        jvmTarget = JvmTarget.JVM_24
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

    val repositoryOwner = System.getenv("REPOSITORY_OWNER") ?: "schaka"
    val containerImageName = "ghcr.io/$repositoryOwner/${project.name}"

    val imageType = System.getenv("IMAGE_TYPE") ?: "jvm"
    val platform = System.getenv("TARGET_PLATFORM") ?: "amd64"
    val baseTag = "$imageType-$platform"

    val containerImageTags = listOf("$containerImageName:$baseTag", "$containerImageName:$baseTag-$shortCommit", "$containerImageName:$baseTag-$branch")

    project.extra["docker.image.name"] = containerImageName
    project.extra["docker.image.version"] = branch
    project.extra["docker.image.source"] = build.projectSourceRoot()
    project.extra["docker.image.tags"] = containerImageTags

}

tasks.withType<BootRun> {
    jvmArgs(
        arrayOf(
            "-Dspring.config.additional-location=optional:/config/application.yml",
        )
    )
}

tasks.withType<ProcessAot> {
    args(
        "-Dspring.config.additional-location=optional:/config/application.yml",
    )
}

tasks.withType<BootBuildImage> {

    docker.publishRegistry.url = "ghcr.io"
    docker.publishRegistry.username = System.getenv("USERNAME") ?: "INVALID_USER"
    docker.publishRegistry.password = System.getenv("GITHUB_TOKEN") ?: "INVALID_PASSWORD"

    val isNative = System.getenv("IMAGE_TYPE") != "jvm"
    val javaBuildPack = if (isNative ) "paketobuildpacks/java-native-image" else "paketobuildpacks/java"
    val javaVendor = if (isNative ) "paketobuildpacks/graalvm" else "paketobuildpacks/adoptium"

    // the java-tiny image has locale issues, documented here: https://github.com/paketo-buildpacks/native-image/issues/344
    // ironically, it works for native images as long as we pass LC_ALL=en_US.UTF-8 during build time
    // if the native image is built with those parameters, they will be available at runtime, no matter what
    builder = "paketobuildpacks/ubuntu-noble-builder-buildpackless"
    if (isNative) {
        runImage = "paketobuildpacks/ubuntu-noble-run-tiny"
    }
    buildpacks = listOf(
        "paketobuildpacks/environment-variables",
        javaVendor,
        javaBuildPack,
        "./buildpacks/aot-cache",
        "paketobuildpacks/health-checker",
    )
    imageName = project.extra["docker.image.name"] as String
    version = project.extra["docker.image.version"] as String
    tags = project.extra["docker.image.tags"] as List<String>
    createdDate = "now"

    val nativeArguments = mapOf(
        "BP_NATIVE_IMAGE" to "true",
        "BPL_SPRING_AOT_ENABLED" to "true",
        "BP_HEALTH_CHECKER_ENABLED" to "true",
        "BP_JVM_VERSION" to "25",
        "BPE_LC_ALL" to "en_US.UTF-8",
        "LC_ALL" to "en_US.UTF-8",
        "BPE_SPRING_CONFIG_ADDITIONAL_LOCATION" to "optional:/config/application.yml",
        "BP_NATIVE_IMAGE_BUILD_ARGUMENTS" to "-march=compatibility -H:+AddAllCharsets",
    )

    val jvmArguments = mapOf(
        "BP_NATIVE_IMAGE" to "false",
        "BP_JVM_CDS_ENABLED" to "false",
        "BP_SPRING_AOT_ENABLED" to "true",
        "BP_HEALTH_CHECKER_ENABLED" to "true",
        "BP_JVM_VERSION" to "25", // JDK required, because we need the executable to run our AOTCache buildpack
        "BP_JVM_TYPE" to "JDK",
        "LC_ALL" to "en_US.UTF-8",
        "BPE_LC_ALL" to "en_US.UTF-8",
        // these values are logged correctly during build time without BPE_ prefix but then not applied at runtime, so we set environment variables for the JVM
        "BPE_BPL_JVM_THREAD_COUNT" to "50",
        "BPE_BPL_JVM_HEAD_ROOM" to "5",
        "BPE_BPL_JVM_LOADED_CLASS_COUNT" to "15000",
        "BPE_SPRING_CONFIG_ADDITIONAL_LOCATION" to "optional:/config/application.yml",
        "BPE_PREPEND_JAVA_TOOL_OPTIONS" to "-XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders",
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:ReservedCodeCacheSize=50M -Xss300K -XX:AOTCache=/workspace/aot-cache/janitorr.aot -Xlog:cds=info -Xlog:aot=info -Xlog:class+path=info",
    )

    // It would also be possible to set this in the graalVmNative block, but we don't want to overwrite Spring's settings
    environment = if (isNative) nativeArguments else jvmArguments
}