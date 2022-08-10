package com.jacobtread.blaze.packet

import com.jacobtread.blaze.logging.PacketLogger
import com.jacobtread.blaze.tdf.Tdf
import io.netty.buffer.ByteBuf

/**
 * Lazy implementation of packet which loads the contents from the
 * provided buffer only if / when the contents are requested. Used
 * by packets loaded from the network
 *
 * @property component the component of this packet
 * @property command The command of this packet
 * @property error The error value of this packet
 * @property type The type of this packet
 * @property id The id of this packet
 * @property contentBuffer The byte buffer to lazy load the content from
 * @constructor Creates a new lazy buffer packet with the provided values and buffer
 */
class LazyBufferPacket(
    override val component: Int,
    override val command: Int,
    override val error: Int,
    override val type: Int,
    override val id: Int,
    val contentBuffer: ByteBuf,
) : Packet {

    /**
     * The Tdf contents stored inside this packet.
     *
     * In this case the TDFs are obtained by lazy reference
     * because they have to be loaded from the content buffer
     */
    override val content: List<Tdf<*>> by lazy {
        if (contentBuffer.refCnt() < 1) {
            emptyList()
        } else {
            val readerIndex = contentBuffer.readerIndex()
            val values = ArrayList<Tdf<*>>()
            try {
                while (contentBuffer.readableBytes() > 4) {
                    values.add(Tdf.read(contentBuffer))
                }
            } catch (e: Throwable) {
                PacketLogger.error("Failed to read packet contents at index ${contentBuffer.readerIndex()}", e)
                if (values.isNotEmpty()) {
                    PacketLogger.error("Last tdf in contents was: " + values.last())
                }
                throw e
            }
            contentBuffer.readerIndex(readerIndex)
            values
        }
    }

    /**
     * Calculates the size in bytes that a buffer
     * would need to be to fit the contents of this
     * packet.
     *
     * In this case the contents are already encoded
     * into a byte buffer so the size of that buffer
     * is the computed size.
     *
     * @return The size in bytes the buffer needs to be
     */
    override fun computeContentSize(): Int {
        return contentBuffer.readableBytes()
    }


    /**
     * Handles writing the contents of this packet
     * to the provided output buffer.
     *
     * In this case the contents are already encoded
     * in a byte buffer so those bytes are directly
     * copied to the output buffer.
     *
     * @param out The buffer to write to
     */
    override fun writeContent(out: ByteBuf) {
        out.writeBytes(contentBuffer, contentBuffer.readerIndex(), contentBuffer.readableBytes())
    }

    override fun toString(): String {
        return "LazyBufferPacket (Component: $component, Command: $command, Error; $error, QType: $type, Id: $id, Content: ${contentBuffer.readableBytes()}byte(s))"
    }
}