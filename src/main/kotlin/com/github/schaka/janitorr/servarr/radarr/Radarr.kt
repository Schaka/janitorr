package com.github.schaka.janitorr.servarr.radarr

import org.springframework.beans.factory.annotation.Qualifier

@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier("radarr")
annotation class Radarr()
