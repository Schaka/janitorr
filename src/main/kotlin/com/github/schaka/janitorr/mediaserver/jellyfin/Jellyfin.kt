package com.github.schaka.janitorr.mediaserver.jellyfin

import org.springframework.beans.factory.annotation.Qualifier

@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier("jellyfin")
annotation class Jellyfin
