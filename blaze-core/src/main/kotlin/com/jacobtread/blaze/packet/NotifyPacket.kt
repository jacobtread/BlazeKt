package com.jacobtread.blaze.packet

import com.jacobtread.blaze.tdf.Tdf
import io.netty.buffer.ByteBuf

class NotifyPacket(
    override val component: Int,
    override val command: Int,
    override val content: List<Tdf<*>>,
) : Packet {

    override val id: Int get() = 0
    override val type: Int get() = Packet.NOTIFY_TYPE
    override val error: Int get() = 0

    override fun writeContent(out: ByteBuf) {
        content.forEach { it.writeFully(out) }
    }

    override fun computeContentSize(): Int {
        var size = 0
        content.forEach { size += it.computeFullSize() }
        return size
    }

    override fun toString(): String {
        return "NotifyPacket (Component: $component, Command: $command, Content Count: ${content.size})"
    }
}
