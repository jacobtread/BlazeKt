package com.jacobtread.blaze

import com.jacobtread.blaze.packet.Packet

/**
 * PacketPushable Represents a class that can have packets
 * sent to it. This is implemented on sessions as well as
 * the networking handler so that packets can be easily sent
 *
 * @constructor Create empty PacketPushable
 */
interface PacketPushable {

    /**
     * push a new packet out to the client
     *
     * @param packet The packet to push
     */
    fun push(packet: Packet)

    fun pushAll(vararg packets: Packet) {
        packets.forEach { push(it) }
    }
}