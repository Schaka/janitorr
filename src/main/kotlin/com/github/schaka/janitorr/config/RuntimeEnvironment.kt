package com.github.schaka.janitorr.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.nio.charset.Charset

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class RuntimeEnvironment {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }


    @PostConstruct
    fun init() {
        log.info("Default charset {}", Charset.defaultCharset().displayName())
        log.info("sun.jnu.encoding {}", System.getProperty("sun.jnu.encoding"))
        log.info("sun.stdout.encoding {}", System.getProperty("sun.stdout.encoding"))
        log.info("sun.stderr.encoding {}", System.getProperty("sun.stderr.encoding"))
        log.info("ENV JAVA_TOOL_OPTIONS {}", System.getenv("JAVA_TOOL_OPTIONS"))
        log.info("ENV LANG {}", System.getenv("LANG"))
        log.info("ENV LANGUAGE {}", System.getenv("LANGUAGE"))
        log.info("ENV LC_CTYPE {}", System.getenv("LC_CTYPE"))
        log.info("ENV LC_ALL {}", System.getenv("LC_ALL"))
    }
}