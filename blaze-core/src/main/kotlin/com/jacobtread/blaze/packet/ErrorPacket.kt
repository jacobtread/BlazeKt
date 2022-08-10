package com.jacobtread.blaze.packet

import com.jacobtread.blaze.tdf.Tdf

class ErrorPacket(
    component: Int,
    command: Int,
    id: Int,
    override val error: Int,
    content: List<Tdf<*>>,
) : ResponsePacket(component, command, id, content) {
    override val type: Int get() = Packet.ERROR_TYPE

    override fun toString(): String {
        return "ErrorPacket (Component: $component, Command: $command, Error: $error, Id: $id, Content Count: ${content.size})"
    }
}
