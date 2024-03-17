package com.github.schaka.janitorr.mediaserver.emby

import org.springframework.beans.factory.annotation.Qualifier

@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier("emby")
annotation class Emby
