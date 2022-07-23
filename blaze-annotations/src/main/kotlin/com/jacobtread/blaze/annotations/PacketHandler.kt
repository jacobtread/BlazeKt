package com.jacobtread.blaze.annotations

/**
 * PacketHandler Marks a function that should handle a
 * specific command for a specific component
 *
 * @property component The component to handle
 * @property command The command to handle
 * @constructor Create empty PacketHandler
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class PacketHandler(
    val component: Int,
    val command:Int
)
