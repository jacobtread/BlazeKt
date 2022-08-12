package com.jacobtread.blaze.packet

import com.jacobtread.blaze.tdf.Tdf

class ResponsePacket(
    override val component: Int,
    override val command: Int,
    override val id: Int,
    override val content: List<Tdf<*>>,
) : Packet {
    override val type: Int get() = Packet.RESPONSE_TYPE
    override val error: Int get() = 0

    override fun toString(): String {
        return "ResponsePacket (Component: $component, Command: $command, Id: $id, Content Count: ${content.size})"
    }
}
