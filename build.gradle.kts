
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
    id("org.springframework.boot") version "4.0.1"
    id("org.springframework.boot.aot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("net.nemerosa.versioning") version "3.1.0"

    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"

}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springframework.boot:spring-boot-starter-kotlinx-serialization-json")
    implementation("org.springframework.boot:spring-boot-webmvc")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("io.github.openfeign:feign-core:13.6")
    implementation("io.github.openfeign:feign-jackson:13.6")
    implementation("io.github.openfeign:feign-httpclient:13.6")

    implementation("org.slf4j:jcl-over-slf4j")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.7")
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
    sourceCompatibility = JavaVersion.VERSION_25.toString()
    targetCompatibility = JavaVersion.VERSION_25.toString()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        jvmTarget = JvmTarget.JVM_25
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
    val containerImageName = "ghcr.io/${repositoryOwner.lowercase()}/${project.name}"

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

    // "paketobuildpacks/builder-noble-java-tiny" has issues with locale, we can work around that by patching the JDK, but I'd rather not
    builder = "paketobuildpacks/ubuntu-noble-builder-buildpackless"
    buildpacks = listOf(
        "paketobuildpacks/environment-variables",
        "paketobuildpacks/adoptium",
        "paketobuildpacks/java",
        "./buildpacks/aot-cache",
    )
    imageName = project.extra["docker.image.name"] as String
    version = project.extra["docker.image.version"] as String
    tags = project.extra["docker.image.tags"] as List<String>
    createdDate = "now"

    environment = mapOf(
        "BP_NATIVE_IMAGE" to "false",
        "BP_JVM_CDS_ENABLED" to "false",
        "BP_SPRING_AOT_ENABLED" to "true",
        "BP_JVM_VERSION" to "25", // JDK required, because we need the executable to run our AOTCache buildpack
        "BP_JVM_TYPE" to "JDK",
        "LC_ALL" to "en_US.UTF-8",
        "BPE_LC_ALL" to "en_US.UTF-8",
        // these values are logged correctly during build time without BPE_ prefix but then not applied at runtime, so we set environment variables for the JVM
        "BPE_BPL_JVM_THREAD_COUNT" to "30",
        "BPE_BPL_JVM_HEAD_ROOM" to "1",
        "BPE_BPL_JVM_LOADED_CLASS_COUNT" to "15000",
        "BPE_SPRING_CONFIG_ADDITIONAL_LOCATION" to "optional:/config/application.yml",
        "BPE_PREPEND_JAVA_TOOL_OPTIONS" to "-XX:+UseSerialGC -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders",
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:ReservedCodeCacheSize=30M -Xss200K -XX:AOTCache=/workspace/aot-cache/janitorr.aot -Xlog:cds=info -Xlog:aot=info -Xlog:class+path=info",
    )
}