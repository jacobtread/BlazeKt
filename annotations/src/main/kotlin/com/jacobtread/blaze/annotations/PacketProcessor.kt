package com.jacobtread.blaze.annotations

/**
 * PacketProcessor Annotation placed on interfaces that should have
 * implementation classes created for processing packets
 *
 * @constructor Create empty PacketHandler
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class PacketProcessor