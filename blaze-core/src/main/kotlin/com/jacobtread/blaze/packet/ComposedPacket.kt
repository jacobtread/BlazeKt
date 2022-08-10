package com.jacobtread.blaze.packet

import com.jacobtread.blaze.tdf.Tdf
import io.netty.buffer.ByteBuf

/**
 * Composed packet implementation represents a packet composed on the
 * server side where all values are already known so no buffer needs
 * to be created or read to get its content values
 *
 * @property component the component of this packet
 * @property command The command of this packet
 * @property error The error value of this packet
 * @property type The type of this packet
 * @property id The id of this packet
 * @property content The packet tdf contents
 * @constructor Creates a new packet with the provided values
 */
class ComposedPacket(
    override val component: Int,
    override val command: Int,
    override val error: Int,
    override val type: Int,
    override val id: Int,
    override val content: List<Tdf<*>>,
) : Packet {
    override fun writeContent(out: ByteBuf) {
        content.forEach { it.writeFully(out) }
    }

    override fun computeContentSize(): Int {
        var size = 0
        content.forEach { size += it.computeFullSize() }
        return size
    }

    override fun toString(): String {
        return "ComposedPacket (Component: $component, Command: $command, Error; $error, QType: $type, Id: $id, Content Count: ${content.size})"
    }
}