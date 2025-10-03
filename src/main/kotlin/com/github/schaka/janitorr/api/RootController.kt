package com.github.schaka.janitorr.api

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Profile("!leyden")
@Controller
class RootController {

    @GetMapping("/")
    fun index(): String {
        return "forward:/index.html"
    }
}
