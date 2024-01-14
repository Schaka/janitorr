package com.github.schaka.janitorr.torrent.transmission

import org.springframework.beans.factory.annotation.Qualifier

@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier("transmission")
annotation class Transmission()
