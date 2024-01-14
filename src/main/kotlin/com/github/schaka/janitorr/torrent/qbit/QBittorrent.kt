package com.github.schaka.janitorr.torrent.qbit

import org.springframework.beans.factory.annotation.Qualifier

@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier("qbittorrent")
annotation class QBittorrent()
