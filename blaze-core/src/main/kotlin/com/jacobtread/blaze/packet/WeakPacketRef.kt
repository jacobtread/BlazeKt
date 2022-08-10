package com.jacobtread.blaze.packet

/**
 * Weak reference for the details of a packet.
 *
 * @property component The component of the packet
 * @property command The command of the packet
 * @property error The error of the packet
 * @property type The type of the packet
 * @property id The id of the packet
 * @constructor Create a weak reference
 */
data class WeakPacketRef(
    val component: Int,
    val command: Int,
    val error: Int,
    val type: Int,
    val id: Int,
) {
    constructor(packet: Packet) : this(packet.component, packet.command, packet.error, packet.type, packet.id)
}