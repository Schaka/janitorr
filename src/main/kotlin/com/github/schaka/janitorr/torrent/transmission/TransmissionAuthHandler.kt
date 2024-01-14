package com.github.schaka.janitorr.torrent.transmission

import com.github.schaka.janitorr.torrent.rest.TorrentClientProperties
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class TransmissionAuthHandler(
        val properties: TorrentClientProperties,
        var lastSessionId: String = ""
) : ClientHttpRequestInterceptor {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers["X-Transmission-Session-Id"] = lastSessionId
        val response = execution.execute(request, body)

        if (response.statusCode == HttpStatus.CONFLICT) {
            lastSessionId = response.headers["X-Transmission-Session-Id"]?.get(0)
                ?: throw IllegalStateException("Can't find Transmission session id in response: $response")
            request.headers["X-Transmission-Session-Id"] = lastSessionId
            return execution.execute(request, body)
        }

        return response
    }
}