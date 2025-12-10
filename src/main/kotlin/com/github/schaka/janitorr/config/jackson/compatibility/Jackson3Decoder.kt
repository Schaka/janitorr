package com.github.schaka.janitorr.config.jackson.compatibility

import feign.Response
import feign.codec.Decoder
import tools.jackson.databind.json.JsonMapper
import java.lang.reflect.Type

class Jackson3Decoder(
    val mapper: JsonMapper
) : Decoder {

    override fun decode(response: Response, type: Type): Any {
        val body = response.body().asInputStream()
        return mapper.readValue(body, mapper.constructType(type))
    }
}