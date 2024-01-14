package com.github.schaka.build

import org.gradle.api.Project
import java.lang.System.getenv
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class Build(private val project: Project) {

    /**
     * The current build time.
     */
    val buildDateAndTime: OffsetDateTime = OffsetDateTime.now()

    /**
     * @return true, if it is a build on a CI system.
     */
    fun isCI(): Boolean {
        return getenv("CI") != null
    }

    /**
     * @return true, if it is a merge request pipeline.
     */
    fun isMergeRequest(): Boolean {

        val ciPipelineSource = getenv("CI_PIPELINE_SOURCE")

        return isCI()
                && ciPipelineSource != null
                && ciPipelineSource == "merge_request_event"
    }

    fun mergeRequestId(): String {
        return getenv("CI_MERGE_REQUEST_IID")
    }

    fun mergeRequestSourceBranch(): String {
        return getenv("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME")
    }

    fun mergeRequestTargetBranch(): String {
        return getenv("CI_MERGE_REQUEST_TARGET_BRANCH_NAME")
    }

    fun branchName(): String {
        return getenv("GITHUB_REF_NAME")
    }

    fun commitHash(): String {
        return getenv("GITHUB_SHA") ?: "local"
    }

    /**
     * @return the username to push containers to the project’s GitLab Container Registry.
     *
     * Only available if the Container Registry is enabled for the project.
     *
     * If the variable is not set, an empty string is used.
     */
    fun containerRegistryUser(): String {
        return getenv("DOCKERHUB_USER") ?: ""
    }

    /**
     * @return The password to push containers to the project’s GitLab Container Registry.
     *
     * Only available if the Container Registry is enabled for the project.
     *
     * This password value is the same as the `CI_JOB_TOKEN` and is valid only as long as the job is running.
     *
     * If the variable is not set, an empty string is used.
     */
    fun containerRegistryPassword(): String {
        return getenv("DOCKERHUB_PASSWORD") ?: ""
    }

    /**
     * @return the container image name to be used as FROM image for the application image.
     */
    fun containerBaseImage(): String {
        return project.property("containerBaseImage") as String
    }

    /**
     * @return the exposed ports by the container.
     */
    fun containerPorts(): List<String> {
        val property = project.property("containerPorts") as String
        return property.split(";").map { it.trim() }
    }

    /**
     * @return the flags for the JVM at runtime.
     */
    fun containerJvmFlags(): List<String> {
        val property = project.property("containerJvmFlags") as String
        return property.split(";").map { it.trim() }
    }

    /**
     * @return the HTTP(S) address of the project.
     *
     * If the variable is not set, an empty string is used.
     */
    fun projectSourceRoot(): String {
        return getenv("CI_PROJECT_URL") ?: "${project.rootDir}"
    }

    /**
     * @return the name of the user who started the job.
     *
     * If the variable is not set, the local user name is used.
     */
    fun userName(): String {
        return getenv("GITLAB_USER_NAME") ?: System.getProperty("user.name")
    }

    /**
     * @return a token to authenticate with certain API endpoints. The token is valid as long as the job is running.
     */
    fun jobToken(): String? {
        return getenv("GITHUB_TOKEN")
    }

    /**
     * @return the current build date in ISO format.
     */
    fun formattedBuildDate(): String {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(buildDateAndTime)
    }

    /**
     * @return the current build time without date in ISO format.
     */
    fun formattedBuildTime(): String {
        return DateTimeFormatter.ofPattern("HH:mm:ss.SSSZ").format(buildDateAndTime)
    }
}