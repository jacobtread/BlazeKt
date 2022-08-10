package com.jacobtread.blaze.packet

import com.jacobtread.blaze.tdf.Tdf
import io.netty.buffer.ByteBuf

open class RequestPacket(
    override val component: Int,
    override val command: Int,
    override val id: Int,
    override val content: List<Tdf<*>>,
) : Packet {

    override val type: Int get() = Packet.REQUEST_TYPE
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
        return "ResponsePacket (Component: $component, Command: $command, Id: $id, Content Count: ${content.size})"
    }
}
