package com.github.schaka.janitorr.setup

import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path

class JellyfinSetup(private val baseUrl: String, private val apiKeyFile: Path) {

    companion object {
        private val log = LoggerFactory.getLogger(JellyfinSetup::class.java)
        private const val ADMIN_USER = "admin"
        private const val ADMIN_PASS = "adminadmin"
        private const val AUTH_PARAMS =
            """Client="Janitorr-LocalDev", Device="LocalDev", DeviceId="00000000-0000-0000-0000-000000000001", Version="1.0.0""""
    }

    private val http = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build()
    private var userToken = ""

    fun setup(): String {
        val cachedKey = loadCachedApiKey()
        if (cachedKey != null) {
            log.info("Using persisted Jellyfin API key")
            addLibraries(cachedKey)
            triggerScan(cachedKey)
            return cachedKey
        }

        if (wizardNeeded()) {
            log.info("Completing Jellyfin startup wizard...")
            completeWizard()
        } else {
            log.info("Jellyfin wizard already complete, skipping setup")
        }

        log.info("Authenticating as admin...")
        authenticateWithRetry()

        log.info("Creating API key...")
        val apiKey = createApiKey()
        saveApiKey(apiKey)

        log.info("Adding media libraries...")
        addLibraries(apiKey)

        log.info("Triggering library scan...")
        triggerScan(apiKey)

        return apiKey
    }

    private fun loadCachedApiKey(): String? {
        if (!Files.exists(apiKeyFile)) return null
        val key = Files.readString(apiKeyFile).trim()
        if (key.isBlank()) return null
        val (status, _) = get("/Users/Me", token = key)
        return if (status in 200..299) key else null
    }

    private fun saveApiKey(apiKey: String) {
        Files.writeString(apiKeyFile, apiKey)
    }

    private fun wizardNeeded(): Boolean {
        repeat(30) { attempt ->
            val (status, _) = get("/Startup/Configuration")
            when {
                status in 200..299 -> {
                    log.info("Jellyfin wizard API ready after {} attempt(s)", attempt + 1)
                    return true
                }
                status == 503 || status < 0 -> {
                    log.info("Jellyfin not ready yet (status {}), waiting... ({}/30)", status, attempt + 1)
                    Thread.sleep(2_000)
                }
                else -> {
                    log.info("Jellyfin wizard already complete (status {})", status)
                    return false
                }
            }
        }
        error("Jellyfin wizard API did not become ready after 30 attempts")
    }

    private fun completeWizard() {
        postAndCheck("/Startup/Configuration", """{"UICulture":"en-US","MetadataCountryCode":"US","PreferredMetadataLanguage":"en"}""")
        val (getUserStatus, _) = get("/Startup/User")
        log.info("GET /Startup/User → {}", getUserStatus)
        postAndCheck("/Startup/User", """{"Name":"$ADMIN_USER","Password":"$ADMIN_PASS"}""")
        postAndCheck("/Startup/RemoteAccess", """{"EnableRemoteAccess":true,"EnableAutomaticPortMapping":false}""")
        postAndCheck("/Startup/Complete", "")
        Thread.sleep(3_000)
    }

    private fun postAndCheck(path: String, body: String) {
        val (status, responseBody) = post(path, body)
        if (status !in 200..299) {
            log.warn("POST {} returned {} — body: {}", path, status, responseBody)
        } else {
            log.info("POST {} → {}", path, status)
        }
    }

    private fun authenticateWithRetry() {
        var lastStatus = -1
        var lastBody = ""
        repeat(10) { attempt ->
            try {
                val (status, body) = post(
                    "/Users/AuthenticateByName",
                    """{"Username":"$ADMIN_USER","Pw":"$ADMIN_PASS"}"""
                )
                lastStatus = status
                lastBody = body
                userToken = parseJsonString(body, "AccessToken")
                if (userToken.isNotBlank()) {
                    log.info("Authenticated successfully")
                    return
                }
                log.warn("Auth attempt {}/10 — status: {}, body: {}", attempt + 1, status, body)
            } catch (e: Exception) {
                log.warn("Auth attempt {}/10 failed with exception: {}", attempt + 1, e.message)
            }
            Thread.sleep(2_000)
        }
        error("Failed to authenticate with Jellyfin after 10 attempts — last status: $lastStatus, body: $lastBody")
    }

    private fun createApiKey(): String {
        post("/Auth/Keys?app=janitorr-local", "", token = userToken)
        val (_, body) = get("/Auth/Keys", token = userToken)
        val key = parseJsonString(body, "AccessToken")
        check(key.isNotBlank()) { "Could not parse API key from Jellyfin response: $body" }
        return key
    }

    private fun addLibraries(apiKey: String) {
        val (_, existingBody) = get("/Library/VirtualFolders", token = apiKey)

        if (!existingBody.contains(""""Name":"Movies"""")) {
            post(
                "/Library/VirtualFolders?name=Movies&collectionType=movies&refreshLibrary=false&paths=%2Fdata%2Fmovies",
                "",
                token = apiKey
            )
            log.info("Movies library added")
        } else {
            log.info("Movies library already exists, skipping")
        }

        if (!existingBody.contains(""""Name":"TV%20Shows"""") && !existingBody.contains(""""Name":"TV Shows"""")) {
            post(
                "/Library/VirtualFolders?name=TV%20Shows&collectionType=tvshows&refreshLibrary=false&paths=%2Fdata%2FTV%20Shows",
                "",
                token = apiKey
            )
            log.info("TV Shows library added")
        } else {
            log.info("TV Shows library already exists, skipping")
        }
    }

    private fun triggerScan(apiKey: String) {
        post("/Library/Refresh", "", token = apiKey)
    }

    private fun post(
        path: String,
        body: String,
        token: String? = null,
    ): Pair<Int, String> {
        val builder = HttpRequest.newBuilder(URI.create("$baseUrl$path"))
            .header("Content-Type", "application/json")
        val authHeader = if (token != null) {
            """MediaBrowser Token="$token", $AUTH_PARAMS"""
        } else {
            "MediaBrowser , $AUTH_PARAMS"
        }
        builder.header("Authorization", authHeader)
        builder.POST(
            if (body.isEmpty()) HttpRequest.BodyPublishers.ofString("{}")
            else HttpRequest.BodyPublishers.ofString(body)
        )
        return try {
            val response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString())
            response.statusCode() to (response.body() ?: "")
        } catch (e: IOException) {
            log.debug("POST {} failed with transport error: {}", path, e.message)
            -1 to ""
        }
    }

    private fun get(path: String, token: String? = null): Pair<Int, String> {
        val builder = HttpRequest.newBuilder(URI.create("$baseUrl$path")).GET()
        val authHeader = if (token != null) {
            """MediaBrowser Token="$token", $AUTH_PARAMS"""
        } else {
            "MediaBrowser , $AUTH_PARAMS"
        }
        builder.header("Authorization", authHeader)
        return try {
            val response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString())
            response.statusCode() to (response.body() ?: "")
        } catch (e: IOException) {
            log.debug("GET {} failed with transport error: {}", path, e.message)
            -1 to ""
        }
    }

    private fun parseJsonString(json: String, key: String): String {
        val marker = """"$key":""""
        val start = json.indexOf(marker)
        if (start == -1) return ""
        val valueStart = start + marker.length
        val end = json.indexOf('"', valueStart)
        return if (end == -1) "" else json.substring(valueStart, end)
    }
}
