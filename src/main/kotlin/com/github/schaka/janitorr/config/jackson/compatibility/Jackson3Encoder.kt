package com.github.schaka.janitorr.config.jackson.compatibility

import feign.RequestTemplate
import feign.codec.Encoder
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import tools.jackson.databind.json.JsonMapper
import java.lang.reflect.Type

class Jackson3Encoder(
    val mapper: JsonMapper
) : Encoder {

    override fun encode(obj: Any, type: Type, template: RequestTemplate) {
        template.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        template.body(mapper.writeValueAsBytes(obj), Charsets.UTF_8)
    }
}