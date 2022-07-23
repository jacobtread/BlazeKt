package com.jacobtread.blaze.packet

import com.jacobtread.blaze.TdfContainer
import com.jacobtread.blaze.tdf.Tdf
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator

interface Packet : TdfContainer {

    companion object {
        const val INCOMING_TYPE = 0x0000 // Packet type representing client -> server
        const val RESPONSE_TYPE = 0x1000 // Packet type representing a response to client -> server from server
        const val UNIQUE_TYPE = 0x2000 // Packet type representing a server -> client message that's not a response
        const val ERROR_TYPE = 0x3000 // Packet type representing a packet with an error
        const val NO_ERROR = 0 // No error error code

        fun allocateBuffer(alloc: ByteBufAllocator, packet: Packet): ByteBuf {
            val contentSize = packet.computeContentSize()
            val bufferSize = 12 + contentSize + (if (contentSize > 0xFFFF) 2 else 0)
            return alloc.ioBuffer(bufferSize, bufferSize)
        }
        fun release(packet: Packet) {
            if (packet is LazyBufferPacket) {
                packet.release()
            }
        }
    }

    val component: Int
    val command: Int
    val error: Int
    val type: Int
    val id: Int

    val content: List<Tdf<*>>

    fun computeContentSize(): Int

    fun writeContent(out: ByteBuf)

    fun writeTo(out: ByteBuf) {
        val contentSize = computeContentSize()
        val isExtended = contentSize > 0xFFFF
        with(out) {
            writeShort(contentSize)
            writeShort(component)
            writeShort(command)
            writeShort(error)
            writeByte(type shr 8)
            writeByte(if (isExtended) 0x10 else 0x00)
            writeShort(id)
            if (isExtended) {
                writeByte(((contentSize.toLong() and 0xFF000000) shr 24).toInt())
                writeByte((contentSize and 0x00FF0000) shr 16)
            }
            writeContent(out)
        }
    }

    override fun getTdfByLabel(label: String): Tdf<*>? = content.find { it.label == label }
}