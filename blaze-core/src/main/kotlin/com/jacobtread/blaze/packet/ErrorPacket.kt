package com.jacobtread.blaze.packet

import com.jacobtread.blaze.tdf.Tdf

class ErrorPacket(
    override val component: Int,
    override val command: Int,
    override val id: Int,
    override val error: Int,
    override val content: List<Tdf<*>>,
) : Packet {
    override val type: Int get() = Packet.ERROR_TYPE

    override fun toString(): String {
        return "ErrorPacket (Component: $component, Command: $command, Error: $error, Id: $id, Content Count: ${content.size})"
    }
}
