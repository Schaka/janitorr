package com.github.schaka.janitorr

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/hook")
class TestController(
        val schedule: CleanupSchedule
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @GetMapping("/test")
    fun sonarr(): ResponseEntity<Any> {
        schedule.runSchedule()
        return ResponseEntity.noContent().build()
    }
}