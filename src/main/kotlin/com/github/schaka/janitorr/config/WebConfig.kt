package com.github.schaka.janitorr.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Forward root path to index.html for Management UI
        registry.addViewController("/").setViewName("forward:/index.html")
    }
}
