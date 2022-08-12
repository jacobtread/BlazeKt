package com.jacobtread.blaze.packet

import com.jacobtread.blaze.tdf.Tdf

class NotifyPacket(
    override val component: Int,
    override val command: Int,
    override val content: List<Tdf<*>>,
) : Packet {
    override val id: Int get() = 0
    override val type: Int get() = Packet.NOTIFY_TYPE
    override val error: Int get() = 0

    override fun toString(): String {
        return "NotifyPacket (Component: $component, Command: $command, Content Count: ${content.size})"
    }
}
