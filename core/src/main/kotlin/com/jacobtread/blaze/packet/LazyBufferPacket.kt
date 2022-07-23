package com.jacobtread.blaze.packet

import com.jacobtread.blaze.PacketLogger
import com.jacobtread.blaze.tdf.Tdf
import io.netty.buffer.ByteBuf

/**
 * Lazy implementation of packet which loads the contents from the
 * provided buffer only if / when the contents are requested. Used
 * by packets loaded from the network
 *
 * This implementation is reference counted and the reference count is
 * linked to the reference count of the underlying content buffer. This
 * reference count should be decremented as soon as the object is done with
 *
 * @see ComposedPacket Packets generated on the server side represented
 * as a list of Tdfs for the value rather than a buffer.
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

    override fun writeContent(out: ByteBuf) {
        out.writeBytes(contentBuffer, contentBuffer.readerIndex(), contentBuffer.readableBytes())
    }

    override fun computeContentSize(): Int = contentBuffer.readableBytes()

    fun release() {
        if (contentBuffer.refCnt() > 0) {
            contentBuffer.release()
        }
    }


    override fun toString(): String {
        return "LazyBufferPacket (Component: $component, Command: $command, Error; $error, QType: $type, Id: $id, Content: ${contentBuffer.readableBytes()}byte(s))"
    }

}